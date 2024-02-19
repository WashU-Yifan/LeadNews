package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constants.WemediaConstants;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.NewsAuthPageReqDto;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.model.wemedia.vos.WmAuthNewsVO;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.NewsAuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NewsAuthServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements NewsAuthService {

    @Autowired
    private WmUserMapper wmUserMapper;

    /**
     * 管理员端查看文章
     * @param dto
     * @return
     */
    public ResponseResult authList(NewsAuthPageReqDto dto) {
        dto.checkParam();

        //2 分页条件查询
        IPage page = new Page(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<WmNews> lambdaQueryWrapper = Wrappers.<WmNews>lambdaQuery();

        //状态
        if (dto.getStatus() != null) {
            lambdaQueryWrapper.eq(WmNews::getStatus, dto.getStatus());
        }

        //关键字模糊查询
        if (StringUtils.isNotBlank(dto.getTitle())) {
            lambdaQueryWrapper.like(WmNews::getTitle, dto.getTitle());
        }

        //创建时间降序
        lambdaQueryWrapper.orderByDesc(WmNews::getCreatedTime);

        page = page(page, lambdaQueryWrapper);

        ResponseResult responseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int)page.getTotal());
        responseResult.setData(page.getRecords());

        return responseResult;
    }

    /**
     * 查看文章内容
     * @param id
     * @return
     */
    public ResponseResult authNew(Integer id){
        if (id == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //获取文章内容
        WmNews wmNews = getById(id);
        if (wmNews == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }

        WmAuthNewsVO wmAuthNewsVO = new WmAuthNewsVO();
        BeanUtils.copyProperties(wmNews, wmAuthNewsVO);
        //获取作者名
        WmUser user = wmUserMapper.selectById(wmAuthNewsVO.getUserId());
        if (user != null) {
            wmAuthNewsVO.setAuthorName(user.getName());
        }

        return ResponseResult.okResult(wmAuthNewsVO);
    }

    /**
     * 人工审核失败
     * @param dto
     * @return
     */
    public ResponseResult authFail(NewsAuthPageReqDto dto) {
        if (dto.getId() == null || dto.getMsg() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        WmNews wmNews = getById(dto.getId());

        wmNews.setStatus(WemediaConstants.WM_STATUS_FAIL);
        wmNews.setReason(dto.getMsg());
        updateById(wmNews);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 人工审核成功
     * @param dto
     * @return
     */
    public ResponseResult authPass(NewsAuthPageReqDto dto) {
        if (dto.getId() == null || dto.getMsg() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        WmNews wmNews = getById(dto.getId());

        wmNews.setStatus(WemediaConstants.WM_STATUS_SUCCESS);
        wmNews.setReason(dto.getMsg());
        updateById(wmNews);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
