package com.heima.article.service;

import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.dtos.ArticleInfoDto;
import com.heima.model.article.dtos.CollectionBehaviorDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.web.bind.annotation.RequestBody;

public interface ApArticleService extends IService<ApArticle> {

    /**
     * 加载文章列表
     * @param articleHomeDto dto
     * @param type 1 加载更多， 2 加载最新
     * @return
     */
    public ResponseResult load(ArticleHomeDto articleHomeDto, Short type);

    /**
     * 加载热点文章列表
     * @param articleHomeDto dto
     * @param type 1 加载更多， 2 加载最新
     * @param firstPage true 是首页， false 非首页
     * @return
     */
    public ResponseResult loadHotArticle(ArticleHomeDto articleHomeDto, Short type, boolean firstPage);

    /**
     * 保存app端相关文章
     * @param dto
     * @return
     */
    public ResponseResult saveArticle(ArticleDto dto);

    /**
     * 收藏文章
     * @param dto
     * @return
     */
    public ResponseResult collectArticle(CollectionBehaviorDto dto);

    /**
     * 返回用户对文章的行为状态
     * @param dto
     * @return
     */
    public ResponseResult loadArticleBehavior(ArticleInfoDto dto);


}
