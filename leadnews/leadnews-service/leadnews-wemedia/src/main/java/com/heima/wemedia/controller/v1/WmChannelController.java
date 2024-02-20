package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmChannelPageReqDto;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.wemedia.service.WmChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/channel")
public class WmChannelController {

    @Autowired
    private WmChannelService wmChannelService;
    /**
     * 查询所有频道
     * @return
     */
    @GetMapping("/channels")
    public ResponseResult findAll() {
        return wmChannelService.findAll();
    }

    @GetMapping("/del/{id}")
    public ResponseResult removeChannel(@PathVariable("id") String id) {
        return wmChannelService.removeChannel(id);
    }

    @PostMapping("/list")
    public ResponseResult findChannelList(@RequestBody WmChannelPageReqDto pageReqDto) {
        return wmChannelService.findChannelList(pageReqDto);
    }

    @PostMapping("/save")
    public ResponseResult saveChannel(@RequestBody WmChannel wmChannel) {
        return wmChannelService.saveChannel(wmChannel);
    }

    /**
     * 更新wmChannel
     * @param wmChannel
     * @return
     */
    @PostMapping("/update")
    public ResponseResult updateChannel(@RequestBody WmChannel wmChannel) {
        return wmChannelService.updateChannel(wmChannel);
    }

}
