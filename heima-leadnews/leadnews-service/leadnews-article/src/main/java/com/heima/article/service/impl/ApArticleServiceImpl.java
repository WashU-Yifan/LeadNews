package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleCollectionMapper;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ApArticleService;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.common.constants.ArticleConstants;
import com.heima.common.constants.BehaviorConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.dtos.ArticleInfoDto;
import com.heima.model.article.dtos.CollectionBehaviorDto;
import com.heima.model.article.pojos.*;
import com.heima.model.article.vos.HotArticleVo;
import com.heima.model.behavior.pojos.Behavior;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
@Slf4j
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {

    @Autowired
    private ApArticleMapper apArticleMapper;

    @Autowired
    private ApArticleCollectionMapper apArticleCollectionMapper;

    @Autowired
    private ApArticleConfigMapper apArticleConfigMapper;

    @Autowired
    private ApArticleContentMapper apArticleContentMapper;

    @Autowired
    private ArticleFreemarkerService articleFreemarkerService;

    @Autowired
    private CacheService cacheService;

    private final static Integer DEFAULT_PAGE_SIZE = 10;
    private final static Integer MAX_PAGE_SIZE = 10;
    /**
     * 加载文章列表
     * @param articleHomeDto dto
     * @param type 1 加载更多， 2 加载最新
     * @return
     */
    @Override
    public ResponseResult load(ArticleHomeDto articleHomeDto, Short type) {
        Integer size = articleHomeDto.getSize();
        if (size == null || size == 0) {
            size = DEFAULT_PAGE_SIZE;
        }
        articleHomeDto.setSize(Math.min(MAX_PAGE_SIZE, size));

        //校验参数 type
        if (type != ArticleConstants.LOADTYPE_LOAD_MORE && type != ArticleConstants.LOADTYPE_LOAD_NEW) {
            type = ArticleConstants.LOADTYPE_LOAD_MORE;
        }

        //校验参数 频道
        if (StringUtils.isBlank(articleHomeDto.getTag())) {
            articleHomeDto.setTag(ArticleConstants.DEFAULT_TAG);
        }

        //校验参数 时间
        if (articleHomeDto.getMaxBehotTime() == null) articleHomeDto.setMaxBehotTime(new Date());
        if (articleHomeDto.getMinBehotTime() == null) articleHomeDto.setMinBehotTime(new Date());

        return ResponseResult.okResult(apArticleMapper.loadArticleList(articleHomeDto, type));
    }


    /**
     * 加载热点文章列表
     * @param articleHomeDto dto
     * @param type 1 加载更多， 2 加载最新
     * @param firstPage true 是首页， false 非首页
     * @return
     */
    public ResponseResult loadHotArticle(ArticleHomeDto articleHomeDto, Short type, boolean firstPage) {
        //首次加载，则使用cache中的热点文章
        if (firstPage) {
            String jsonStr = cacheService.get(ArticleConstants.HOT_ARTICLE_FIRST_PAGE + articleHomeDto.getTag());

            if (StringUtils.isNotBlank(jsonStr)) {
                List<HotArticleVo> articleList = JSON.parseArray(jsonStr, HotArticleVo.class);
                return ResponseResult.okResult(articleList);
            }
        }
        return load(articleHomeDto, type);
    }


    /**
     * 保存app端相关文章
     * @param dto
     * @return
     */
    public ResponseResult saveArticle(ArticleDto dto) {
        //1 parameter checks
        if (dto == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        ApArticle apArticle = new ApArticle();
        BeanUtils.copyProperties(dto, apArticle);

        //2 If article_id exist
        if (dto.getId() == null) {
            //2.1 If not, save the article
            save(apArticle);

            //Article config
            ApArticleConfig apArticleConfig = new ApArticleConfig(apArticle.getId());
            apArticleConfigMapper.insert(apArticleConfig);

            //Article content
            ApArticleContent apArticleContent = new ApArticleContent();
            apArticleContent.setArticleId(apArticle.getId());
            apArticleContent.setContent(dto.getContent());
            apArticleContentMapper.insert(apArticleContent);
        }
        else {
            //2.2 Else, save the modified article
            updateById(apArticle);

            //Article content
            ApArticleContent apArticleContent = apArticleContentMapper.selectOne(Wrappers.<ApArticleContent>lambdaQuery().eq(ApArticleContent::getArticleId, dto.getId()));
            apArticleContent.setContent(dto.getContent());
            apArticleContentMapper.updateById(apArticleContent);
        }

        //Asynchronously push the static file onto minio
        articleFreemarkerService.buildArticleToMinIO(apArticle, dto.getContent());

        return ResponseResult.okResult(apArticle.getId());
    }


    /**
     * 收藏文章
     * @param dto
     * @return
     */
    @Override
    public ResponseResult collectArticle(CollectionBehaviorDto dto) {

        //检查参数
        if (dto.getEntryId() == null || dto.getOperation() == null || dto.getPublishedTime() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        ApArticleCollection apArticleCollection = new ApArticleCollection();
        BeanUtils.copyProperties(dto, apArticleCollection);

        //前后端字符命名存在矛盾
        apArticleCollection.setEntryId(dto.getEquipmentId());
        apArticleCollection.setArticleId(dto.getEntryId());
        apArticleCollection.setCollectionTime(new Date());

        //判断点赞/取消点赞
        String key = BehaviorConstants.COLLECT_BEHAVIOR_KEY + dto.getEntryId().toString();
        if (dto.getOperation() == BehaviorConstants.ARTICLE_COLLECT) {
            Object obj = cacheService.hGet(key, user.getId().toString());
            if (obj != null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "已点赞");
            }
            log.info("保存当前key:{}, {}, {}", key, user.getId(),  apArticleCollection);
            cacheService.hPut(key, user.getId().toString(), JSON.toJSONString(apArticleCollection));

            //保存至mysql
            apArticleCollectionMapper.insert(apArticleCollection);
        }
        else {
            log.info("s删除当前key:{}, {}, {}", key, user.getId(), apArticleCollection);
            cacheService.hDelete(key, user.getId().toString());

        }

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);

    }

    /**
     * 返回用户对文章的行为状态
     * @param dto
     * @return
     */
    @Override
    public ResponseResult loadArticleBehavior(ArticleInfoDto dto) {
        //判断参数
        if (dto.getArticleId() == null || dto.getAuthorId() == null) {
            ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //检查登录
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        Behavior behavior = new Behavior();

        //判断是否点赞
        Object obj = cacheService.hGet(BehaviorConstants.LIKE_BEHAVIOR_KEY + dto.getArticleId(), user.getId().toString());
        if (obj != null) {
            behavior.setIsLike(true);
        }

        //是否不喜欢
        obj = cacheService.hGet(BehaviorConstants.UNLIKE_BEHAVIOR_KEY + dto.getArticleId(), user.getId().toString());
        if (obj != null) {
            behavior.setIsUnlike(true);
        }

        //是否收藏
        obj = cacheService.hGet(BehaviorConstants.COLLECT_BEHAVIOR_KEY + dto.getArticleId(), user.getId().toString());
        if (obj != null) {
            behavior.setIsCollection(true);
        }

        //是否关注
        obj = cacheService.hGet(BehaviorConstants.FOLLOW_BEHAVIOR_KEY + user.getId(), dto.getAuthorId().toString());
        if (obj != null) {
            behavior.setIsFollow(true);
        }

        return ResponseResult.okResult(behavior);
    }
}
