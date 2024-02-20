package com.ethan.admin.controller.v1;

import com.ethan.admin.service.AdLoginService;
import com.heima.model.admin.dtos.AdLoginDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private AdLoginService adLoginService;


    @PostMapping("in")
    public ResponseResult login(@RequestBody AdLoginDto loginDto) {
        return adLoginService.login(loginDto);
    }
}
