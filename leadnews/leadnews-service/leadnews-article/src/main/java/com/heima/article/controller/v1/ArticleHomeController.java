package com.heima.article.controller.v1;

import com.heima.article.service.ApArticleService;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.dtos.ArticleInfoDto;
import com.heima.model.article.dtos.CollectionBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/article")
public class ArticleHomeController {
    @Autowired
    ApArticleService apArticleService;

    @PostMapping("/load")
    public ResponseResult load(@RequestBody ArticleHomeDto articleHomeDto) {
        return apArticleService.loadHotArticle(articleHomeDto, ArticleConstants.LOADTYPE_LOAD_MORE, true);
    }

    @PostMapping("/loadmore")
    public ResponseResult loadMore(@RequestBody ArticleHomeDto articleHomeDto) {
        return apArticleService.load(articleHomeDto, ArticleConstants.LOADTYPE_LOAD_MORE);
    }

    @PostMapping("/loadnew")
    public ResponseResult loadNew(@RequestBody ArticleHomeDto articleHomeDto) {
        return apArticleService.load(articleHomeDto, ArticleConstants.LOADTYPE_LOAD_NEW);
    }

    @PostMapping("/load_article_behavior")
    public ResponseResult loadArticleBehavior(@RequestBody ArticleInfoDto dto) {
        return apArticleService.loadArticleBehavior(dto);
    }


}
