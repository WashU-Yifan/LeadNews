package com.heima.article.service;

import com.heima.model.article.pojos.ApArticle;

public interface ArticleFreemarkerService {

    /**
     * Generate static resource and push onto the remote minIO service
     * @param apArticle
     * @param content
     */
    public void buildArticleToMinIO(ApArticle apArticle, String content);

}
