package com.nh.netty.client.controller;

import com.nh.netty.annotation.RemoteInvoke;
import com.nh.netty.server.service.UserService;
import org.springframework.stereotype.Component;

@Component
public class LoginController {

    @RemoteInvoke
    private UserService userService;

    public Object getUserByName(String userName){
        return userService.getUserByName(userName);
    }
}
