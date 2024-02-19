package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.NewsAuthPageReqDto;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmNews;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface WmNewsService extends IService<WmNews> {

    /**
     * 条件查询文章列表
     * @param wmNewsPageReqDto
     * @return
     */
    public ResponseResult findList(WmNewsPageReqDto wmNewsPageReqDto);

    /**
     * 上传文章 或修改为草稿
     * @param wmNewsDto
     * @return
     */
    public ResponseResult submitNews(WmNewsDto wmNewsDto);


    /**
     * 文章上下架
     * @param wmNewsDto
     * @return
     */
    public ResponseResult upOrDown(WmNewsDto wmNewsDto);

}
