package com.heima.model.user.dtos;

import com.heima.model.common.dtos.PageRequestDto;
import io.swagger.models.auth.In;
import lombok.Data;

@Data
public class AuthPageRequestDto extends PageRequestDto {

    /**
     * 用户id
     */
    private Integer id;

    /**
     * 拒绝原因
     */
    private String msg;

    /**
     * 审核状态
     */
    private Short status;


}
