package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmChannelPageReqDto;
import com.heima.model.wemedia.pojos.WmChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface WmChannelService extends IService<WmChannel> {


    public ResponseResult findAll();

    /**
     * 根据id删除频道
     * @param id
     * @return
     */
    public ResponseResult removeChannel(String id);

    /**
     * 频道模糊分页查询
     * @param pageReqDto
     * @return
     */
    public ResponseResult findChannelList( WmChannelPageReqDto pageReqDto);

    /**
     * 保存wmChannel
     * @param wmChannel
     * @return
     */
    public ResponseResult saveChannel(WmChannel wmChannel);

    /**
     * 更新wmChannel
     * @param wmChannel
     * @return
     */
    public ResponseResult updateChannel(WmChannel wmChannel);
}