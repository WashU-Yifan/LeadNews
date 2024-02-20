package com.heima.user.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.AuthPageRequestDto;
import com.heima.user.service.ApUserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/auth")
@RestController
public class ApUserIdentifyController {

    @Autowired
    private ApUserAuthService apUserAuthService;

    /**
     * 审核用户列表
     * @param authDto
     * @return
     */
    @PostMapping("/list")
    public ResponseResult userList(@RequestBody AuthPageRequestDto authDto) {
        return apUserAuthService.userList(authDto);
    }

    @PostMapping("/authFail")
    public ResponseResult authFail(@RequestBody AuthPageRequestDto authDto) {
        return apUserAuthService.authFail(authDto);
    }

    @PostMapping("/authPass")
    public ResponseResult authSuccess(@RequestBody AuthPageRequestDto authDto) {
        return apUserAuthService.authSuccess(authDto);
    }

}
