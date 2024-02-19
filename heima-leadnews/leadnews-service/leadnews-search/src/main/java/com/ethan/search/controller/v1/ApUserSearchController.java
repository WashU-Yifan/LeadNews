package com.ethan.search.controller.v1;

import com.ethan.search.service.ApUserSearchService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.HistorySearchDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/history")
public class ApUserSearchController {

    @Autowired
    private ApUserSearchService apUserSearchService;

    @PostMapping("/load")
    public ResponseResult findUserSearchHistory() {
        return apUserSearchService.findUserSearchHistory();
    }

    @PostMapping("/del")
    public ResponseResult delUserSearchHistory(HistorySearchDto historySearchDto) {
        return apUserSearchService.delUserSearchHistory(historySearchDto);
    }
}
