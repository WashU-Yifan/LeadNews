package com.ethan.search.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.HistorySearchDto;

public interface ApUserSearchService {

    /**
     * Save user's search history into MongoDB
     * @param keyword
     * @param userId
     */
    public void saveSearchHistory(String keyword, Integer userId);

    /**
     * 获取用户搜索记录历史
     * @return
     */
    public ResponseResult findUserSearchHistory();

    /**
     * 删除搜索历史
     * @param historySearchDto
     * @return
     */
    public ResponseResult delUserSearchHistory(HistorySearchDto historySearchDto);
    }
