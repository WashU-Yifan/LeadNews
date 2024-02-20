package com.ethan.search.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;

public interface ApAssociateWordsService {
    /**
     *
     * @param userSearchDto
     * @return
     */
    public ResponseResult searchAssociate(UserSearchDto userSearchDto);
}
