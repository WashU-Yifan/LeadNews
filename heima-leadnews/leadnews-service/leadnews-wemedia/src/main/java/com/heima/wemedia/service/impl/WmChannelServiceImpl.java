package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmChannelPageReqDto;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.utils.thread.WmThreadLocalUtil;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.service.WmChannelService;
import com.heima.wemedia.service.WmMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional
@Slf4j
public class WmChannelServiceImpl extends ServiceImpl<WmChannelMapper, WmChannel> implements WmChannelService {

    @Override
    public ResponseResult findAll() {
        return ResponseResult.okResult(list());
    }


    /**
     * 根据id删除频道
     * @param id
     * @return
     */
    @Override
    public ResponseResult removeChannel(String id) {
        WmChannel wmChannel = getById(id);
        //必须禁用才可删除
        if (wmChannel == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        if (wmChannel.getStatus()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "频道未禁用");
        }

        removeById(id);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 频道模糊分页查询
     * @param pageReqDto
     * @return
     */
    @Override
    public ResponseResult findChannelList(WmChannelPageReqDto pageReqDto) {
        pageReqDto.checkParam();

        //2 分页条件查询
        IPage page = new Page(pageReqDto.getPage(), pageReqDto.getSize());
        LambdaQueryWrapper<WmChannel> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        //关键字模糊查询
        if (StringUtils.isNotEmpty(pageReqDto.getName())) {
            lambdaQueryWrapper.like(WmChannel::getName, pageReqDto.getName());
        }

        lambdaQueryWrapper.orderByDesc(WmChannel::getCreatedTime);
        page = page(page, lambdaQueryWrapper);

        ResponseResult responseResult = new PageResponseResult(pageReqDto.getPage(), pageReqDto.getSize(), (int)page.getTotal());
        responseResult.setData(page.getRecords());

        return responseResult;
    }


    /**
     * 保存wmChannel
     * @param wmChannel
     * @return
     */
    public ResponseResult saveChannel(WmChannel wmChannel) {
        if (StringUtils.isEmpty(wmChannel.getName()) || StringUtils.isEmpty(wmChannel.getDescription())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        WmChannel one = getOne(Wrappers.<WmChannel>lambdaQuery().eq(WmChannel::getName, wmChannel.getName()));
        if (one != null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST);
        }

        wmChannel.setCreatedTime(new Date());
        save(wmChannel);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 更新wmChannel
     * @param wmChannel
     * @return
     */
    public ResponseResult updateChannel(WmChannel wmChannel) {
        if (StringUtils.isEmpty(wmChannel.getName())
                || StringUtils.isEmpty(wmChannel.getDescription())
                || wmChannel.getId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        WmChannel one = getOne(Wrappers.<WmChannel>lambdaQuery().eq(WmChannel::getName, wmChannel.getName()));
        if (one == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }

        updateById(wmChannel);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}