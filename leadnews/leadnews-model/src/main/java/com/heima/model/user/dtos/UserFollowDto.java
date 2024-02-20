package com.heima.model.user.dtos;

import io.swagger.models.auth.In;
import lombok.Data;

@Data
public class UserFollowDto {

    /**
     * 文章id
     */
    private Long articleId;

    /**
     *  作者id
     */
    private Integer authorId;

    /**
     * 0 点赞 1 取消点赞
     */
    private Short operation;


}
