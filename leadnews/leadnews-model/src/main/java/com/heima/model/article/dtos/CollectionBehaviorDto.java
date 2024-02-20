package com.heima.model.article.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class CollectionBehaviorDto {

    /**
     * 设备id
     */
    private Integer equipmentId;
    /**
     * 文章id
     */
    private Long entryId;

    /**
     * 0 收藏 1 取消收藏
     */
    private Short operation;

    /**
     * 发布时间
     */
    private Date publishedTime;


    /**
     *  0文章
     *  1动态
     */
    private Short type;
}
