package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.NewsAuthPageReqDto;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.wemedia.service.NewsAuthService;
import com.heima.wemedia.service.WmNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/v1/news")
public class WmNewsController {

    @Autowired
    private WmNewsService wmNewsService;

    @Autowired
    private NewsAuthService newsAuthService;

    /**
     * 自媒体端查看文章
     * @param wmNewsPageReqDto
     * @return
     */
    @PostMapping("/list")
    public ResponseResult findList(@RequestBody WmNewsPageReqDto wmNewsPageReqDto) {
        return wmNewsService.findList(wmNewsPageReqDto);
    }

    /**
     * 提交文章
     * @param wmNewsDto
     * @return
     */
    @PostMapping("/submit")
    public ResponseResult submitNews(@RequestBody WmNewsDto wmNewsDto) {
        return wmNewsService.submitNews(wmNewsDto);
    }

    /**
     * 上下架文章
     * @param wmNewsDto
     * @return
     */
    @PostMapping("/down_or_up")
    public ResponseResult upOrDown(@RequestBody WmNewsDto wmNewsDto) {return wmNewsService.upOrDown(wmNewsDto);}


    /**
     * 管理员端查看文章
     * @param dto
     * @return
     */
    @PostMapping("/list_vo")
    public ResponseResult authList(@RequestBody NewsAuthPageReqDto dto) {
        return newsAuthService.authList(dto);
    }

    /**
     * 查看文章内容
     * @param id
     * @return
     */
    @GetMapping("/one_vo/{id}")
    public ResponseResult authNew(@PathVariable("id") Integer id) {
        return newsAuthService.authNew(id);
    }

    /**
     * 人工审核失败
     * @param dto
     * @return
     */
    @PostMapping("/auth_fail")
    public ResponseResult authFail(@RequestBody NewsAuthPageReqDto dto) {
        return newsAuthService.authFail(dto);
    }

    /**
     * 人工审核成功
     * @param dto
     * @return
     */
    @PostMapping("/auth_pass")
    public ResponseResult authPass(@RequestBody NewsAuthPageReqDto dto) {
        return newsAuthService.authPass(dto);
    }

}
