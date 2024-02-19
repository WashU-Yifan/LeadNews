package com.heima.model.wemedia.dtos;

import com.heima.model.common.dtos.PageRequestDto;
import lombok.Data;

@Data
public class NewsAuthPageReqDto extends PageRequestDto {

    /**
     * 文章id
     */
    private Integer id;

    /**
     * 失败原因
     */
    private String msg;

    /**
     *  0 草稿
     *             1 提交（待审核）
     *             2 审核失败
     *             3 人工审核
     *             4 人工审核通过
     *             8 审核通过（待发布）
     *             9 已发布
     */
    private Integer status;

    /**
     * 标题模糊查询
     */
    private String title;

}
