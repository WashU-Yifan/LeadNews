package com.heima.wemedia.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constants.WemediaConstants;
import com.heima.common.constants.WmNewsMessageConstants;
import com.heima.common.exception.CustomException;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmNewsMaterial;
import com.heima.utils.thread.WmThreadLocalUtil;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmNewsMaterialMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import com.heima.wemedia.service.WmNewsService;
import com.heima.wemedia.service.WmNewsTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {

    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;

    @Autowired
    private WmMaterialMapper wmMaterialMapper;

    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;

    @Autowired
    private WmNewsTaskService wmNewsTaskService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    /**
     * 条件查询文章列表
     * @param wmNewsPageReqDto
     * @return
     */
    public ResponseResult findList(WmNewsPageReqDto wmNewsPageReqDto) {
        //1 参数检查
        wmNewsPageReqDto.checkParam();

        //2 分页条件查询
        IPage page = new Page(wmNewsPageReqDto.getPage(), wmNewsPageReqDto.getSize());
        LambdaQueryWrapper<WmNews> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        //状态
        if (wmNewsPageReqDto.getStatus() != null) {
            lambdaQueryWrapper.eq(WmNews::getStatus, wmNewsPageReqDto.getStatus());
        }
        //频道
        if (wmNewsPageReqDto.getChannelId() != null) {
            lambdaQueryWrapper.eq(WmNews::getChannelId, wmNewsPageReqDto.getChannelId());
        }
        //创建时间
        if (wmNewsPageReqDto.getBeginPubDate() != null && wmNewsPageReqDto.getEndPubDate() != null) {
            lambdaQueryWrapper.between(WmNews::getCreatedTime, wmNewsPageReqDto.getBeginPubDate(), wmNewsPageReqDto.getEndPubDate());
        }
        //关键字模糊查询
        if (StringUtils.isNotBlank(wmNewsPageReqDto.getKeyword())) {
            lambdaQueryWrapper.like(WmNews::getTitle, wmNewsPageReqDto.getKeyword());
        }
        //用户id
        lambdaQueryWrapper.eq(WmNews::getUserId, WmThreadLocalUtil.getUser().getId());
        lambdaQueryWrapper.orderByDesc(WmNews::getPublishTime);

        page = page(page, lambdaQueryWrapper);

        ResponseResult responseResult = new PageResponseResult(wmNewsPageReqDto.getPage(), wmNewsPageReqDto.getSize(), (int)page.getTotal());
        responseResult.setData(page.getRecords());

        return responseResult;
    }

    /**
     * 上传文章 或修改为草稿
     * @param wmNewsDto
     * @return
     */
    @Override
    public ResponseResult submitNews(WmNewsDto wmNewsDto) {
        if (wmNewsDto == null || wmNewsDto.getContent() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //1 保存或修改文章
        WmNews wmNews = new WmNews();

        try {
            BeanUtils.copyProperties(wmNews, wmNewsDto);
        }
        catch (Exception e) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        if (wmNewsDto.getImages() != null && wmNewsDto.getImages().size() >= 0) {
            String imageStr = StringUtils.join(wmNewsDto.getImages(), ",");
            wmNews.setImages(imageStr);
        }

        //如果图片类型为自动
        if (wmNewsDto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)) {
            wmNews.setType(null);
        }
        saveOrUpdateWmNews(wmNews);

        //2如果为草稿则结束
        if (wmNewsDto.getStatus().equals(WmNews.Status.NORMAL.getCode())) {
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }

        //不是草稿则更新图片与文章的关系
        List<String> materials = extractUrlInfo(wmNewsDto.getContent());
        saveRelativeInfo(materials, wmNews.getId(), WemediaConstants.WM_CONTENT_REFERENCE);


        //4.不是草稿，保存文章封面图片与素材的关系，如果当前布局是自动，需要匹配封面图片
        saveRelativeInfoForCover(wmNewsDto, wmNews, materials);

        //5.审核文章 异步
        //原本为直接审核，不管时间
        //wmNewsAutoScanService.autoScanWmNews(wmNews.getId());

        //先为加入Redis延迟任务队列，只有当publish time 到达才会执行
        wmNewsTaskService.addNewsToTask(wmNews.getId(), wmNews.getPublishTime());
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 保存或修改文章
     * @param wmNews
     */
    private void saveOrUpdateWmNews(WmNews wmNews) {
        wmNews.setUserId(WmThreadLocalUtil.getUser().getId());
        wmNews.setCreatedTime(new Date());
        wmNews.setSubmitedTime(new Date());
        wmNews.setEnable((short)1); //默认上架

        if (wmNews.getId() == null) {
            //新插入
            save(wmNews);
        }
        else {
            //修改
            //删除图片与文章的关系
            wmNewsMaterialMapper.delete(Wrappers.<WmNewsMaterial>lambdaQuery().eq(WmNewsMaterial::getNewsId, wmNews.getId()));
            updateById(wmNews);
        }
    }

    /**
     * 提取文章中的图片url
     * @param content
     * @return
     */
    List<String> extractUrlInfo(String content) {
        List<String> materials = new ArrayList<>();
        List<Map> maps = JSON.parseArray(content, Map.class);

        for (Map map : maps) {
            if (map.get("type").equals("image")) {
                String imgUrl = (String) map.get("value");
                materials.add(imgUrl);
            }
        }
        return materials;
    }

    /**
     * 处理文章内容图片与文章的关系
     * @param materials
     * @param newsId
     */
    private void saveRelativeInfo(List<String> materials, Integer newsId, Short type) {
        if (materials == null || materials.isEmpty()) {
            return;
        }
        List<WmMaterial> wmMaterials = wmMaterialMapper.selectList(Wrappers.<WmMaterial>lambdaQuery().in(WmMaterial::getUrl, materials));

        if (wmMaterials == null || wmMaterials.size() == 0) {
            throw new CustomException(AppHttpCodeEnum.MATERIAL_REFERENCE_FAIL);
        }
        if(wmMaterials.size() != materials.size()){
            throw new CustomException(AppHttpCodeEnum.MATERIAL_REFERENCE_FAIL);
        }
        List<Integer> idList = wmMaterials.stream().map(WmMaterial::getId).collect(Collectors.toList());

        wmNewsMaterialMapper.saveRelations(idList, newsId, type);
    }

    /**
     * 第一个功能：如果当前封面类型为自动，则设置封面类型的数据
     * 匹配规则：
     * 1，如果内容图片大于等于1，小于3  单图  type 1
     * 2，如果内容图片大于等于3  多图  type 3
     * 3，如果内容没有图片，无图  type 0
     *
     * 第二个功能：保存封面图片与素材的关系
     * @param wmNewsDto
     * @param wmNews
     * @param materials
     */
    private void saveRelativeInfoForCover(WmNewsDto wmNewsDto, WmNews wmNews, List<String> materials) {
        List<String> coverImages = wmNewsDto.getImages();

        //如果当前封面类型为自动，则设置封面类型的数据
        if(wmNewsDto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)){
            //多图
            if(materials.size() >= WemediaConstants.WM_NEWS_MANY_IMAGE){
                wmNews.setType(WemediaConstants.WM_NEWS_MANY_IMAGE);
                coverImages = materials.stream().limit(WemediaConstants.WM_NEWS_MANY_IMAGE).collect(Collectors.toList());
            }else if(materials.size() >= WemediaConstants.WM_NEWS_SINGLE_IMAGE && materials.size() < WemediaConstants.WM_NEWS_MANY_IMAGE){
                //单图
                wmNews.setType(WemediaConstants.WM_NEWS_SINGLE_IMAGE);
                coverImages = materials.stream().limit(WemediaConstants.WM_NEWS_SINGLE_IMAGE).collect(Collectors.toList());
            }else {
                //无图
                wmNews.setType(WemediaConstants.WM_NEWS_NONE_IMAGE);
            }

            //修改文章封面图片引用
            if(coverImages != null && coverImages.size() > 0){
                wmNews.setImages(StringUtils.join(coverImages,","));
            }
            updateById(wmNews);
        }
        if(coverImages != null && coverImages.size() > 0){
            saveRelativeInfo(coverImages, wmNews.getId(), WemediaConstants.WM_COVER_REFERENCE);
        }

    }



    /**
     * 文章上下架
     * @param wmNewsDto
     * @return
     */
    public ResponseResult upOrDown(WmNewsDto wmNewsDto) {
        if (wmNewsDto.getId() == null || wmNewsDto.getEnable() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        if(!(wmNewsDto.getEnable() == WemediaConstants.WM_NEWS_UP) && !(wmNewsDto.getEnable() == WemediaConstants.WM_NEWS_DOWN)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        WmNews wmNews = getById(wmNewsDto.getId());
        if (wmNews == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        if(!wmNews.getStatus().equals(WmNews.Status.PUBLISHED.getCode())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "当前文章未发布， 不可上下架");
        }

        //修改上下架字段
        update(Wrappers.<WmNews>lambdaUpdate().
                set(WmNews::getEnable, wmNewsDto.getEnable()).
                eq(WmNews::getId, wmNewsDto.getId()));

        //发送消息到article服务
        if(wmNews.getArticleId() != null){
            Map<String,Object> map = new HashMap<>();
            map.put("articleId",wmNews.getArticleId());
            map.put("enable", wmNewsDto.getEnable());
            kafkaTemplate.send(WmNewsMessageConstants.WM_NEWS_UP_OR_DOWN_TOPIC,JSON.toJSONString(map));
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

}


