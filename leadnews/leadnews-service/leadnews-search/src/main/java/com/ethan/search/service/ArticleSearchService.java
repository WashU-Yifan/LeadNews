package com.ethan.search.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;

public interface ArticleSearchService {

    /**
     * es 文章分页检查
     * @param userSearchDto
     * @return
     */
    public ResponseResult search(UserSearchDto userSearchDto);
}
