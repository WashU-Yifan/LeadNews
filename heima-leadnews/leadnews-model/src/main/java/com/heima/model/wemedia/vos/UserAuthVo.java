package com.heima.model.wemedia.vos;


import lombok.Data;

import java.util.Date;

@Data
public class UserAuthVo {

    /**
     * 账号ID
     */
    private Integer userId;

    /**
     * 真名
     */
    private String name;

    /**
     * 密码,md5加密
     */

    private String password;

    /**
     * 密码、通信等加密盐
     */
    private String salt;

    /**
     * 用户名
     */
    private String nickname;

    /**
     * 头像
     */

    private String image;

    /**
     * 手机号
     */

    private String phone;





}
