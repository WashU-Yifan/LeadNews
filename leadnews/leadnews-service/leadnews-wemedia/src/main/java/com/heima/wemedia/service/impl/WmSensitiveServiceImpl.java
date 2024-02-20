package com.heima.wemedia.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmSensitivePageReqDto;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.service.WmSensitiveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class WmSensitiveServiceImpl extends ServiceImpl<WmSensitiveMapper, WmSensitive> implements WmSensitiveService {


    /**
     * 删除敏感词
     *
     * @param sensitiveId
     * @return
     */

    @Override
    public ResponseResult delSensitive(String sensitiveId) {
        if (StringUtils.isEmpty(sensitiveId)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        removeById(sensitiveId);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }


    /**
     * 分页敏感词查询
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult getSensitiveList(WmSensitivePageReqDto dto) {
        //1 参数检查
        dto.checkParam();

        //2 分页条件查询
        IPage page = new Page(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<WmSensitive> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotEmpty(dto.getName())) {
            queryWrapper.like(WmSensitive::getSensitives, dto.getName());
        }

        queryWrapper.orderByDesc(WmSensitive::getCreatedTime);
        page = page(page, queryWrapper);


        ResponseResult responseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        responseResult.setData(page.getRecords());

        return responseResult;
    }

    @Override
    public ResponseResult saveSensitive(WmSensitive wmSensitive) {
        //1 参数检查
        if (StringUtils.isEmpty(wmSensitive.getSensitives())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //2 查重
        WmSensitive one = getOne(Wrappers.<WmSensitive>lambdaQuery().eq(WmSensitive::getSensitives, wmSensitive));
        if (one != null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST);
        }

        wmSensitive.setCreatedTime(new Date());
        save(wmSensitive);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult updateSensitive(WmSensitive wmSensitive) {
        //1 参数检查
        if (StringUtils.isEmpty(wmSensitive.getSensitives()) || wmSensitive.getId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        updateById(wmSensitive);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

}
