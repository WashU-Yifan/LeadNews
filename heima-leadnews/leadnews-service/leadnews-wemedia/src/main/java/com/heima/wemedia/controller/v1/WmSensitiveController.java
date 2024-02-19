package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmSensitivePageReqDto;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.wemedia.service.WmSensitiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sensitive")
public class WmSensitiveController {

    @Autowired
    private WmSensitiveService wmSensitiveService;

    @PostMapping("/del/{id}")
    public ResponseResult delSensitive(@PathVariable("id")String id) {
        return wmSensitiveService.delSensitive(id);
    }

    @PostMapping("/list")
    public ResponseResult findSensitiveList(@RequestBody WmSensitivePageReqDto dto) {
        return wmSensitiveService.getSensitiveList(dto);
    }

    @PostMapping("/save")
    public ResponseResult saveSensitive(@RequestBody WmSensitive wmSensitive) {
        return wmSensitiveService.saveSensitive(wmSensitive);
    }

    @PostMapping("/update")
    public ResponseResult updateSensitive(@RequestBody WmSensitive wmSensitive) {
        return wmSensitiveService.updateSensitive(wmSensitive);
    }
}
