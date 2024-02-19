package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmSensitivePageReqDto;
import com.heima.model.wemedia.pojos.WmSensitive;

public interface WmSensitiveService extends IService<WmSensitive> {

    /**
     * 删除敏感词
     * @param sensitiveId
     * @return
     */
    public ResponseResult delSensitive(String sensitiveId);


    /**
     * 分页敏感词查询
     * @param dto
     * @return
     */
    public ResponseResult getSensitiveList(WmSensitivePageReqDto dto);


    /**
     * 新增sensitive
     * @param wmSensitive
     * @return
     */
    public ResponseResult saveSensitive(WmSensitive wmSensitive);

    /**
     *  修改敏感词
     * @param wmSensitive
     * @return
     */
    public ResponseResult updateSensitive(WmSensitive wmSensitive);
}
