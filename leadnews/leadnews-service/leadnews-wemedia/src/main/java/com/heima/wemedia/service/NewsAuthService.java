package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.NewsAuthPageReqDto;
import com.heima.model.wemedia.pojos.WmNews;

public interface NewsAuthService extends IService<WmNews> {

    /**
     * 管理员端查看文章
     * @param dto
     * @return
     */
    public ResponseResult authList( NewsAuthPageReqDto dto);

    /**
     * 查看文章内容
     * @param id
     * @return
     */
    public ResponseResult authNew(Integer id);

    /**
     * 人工审核失败
     * @param dto
     * @return
     */
    public ResponseResult authFail(NewsAuthPageReqDto dto);

    /**
     * 人工审核成功
     * @param dto
     * @return
     */
    public ResponseResult authPass(NewsAuthPageReqDto dto);
}
