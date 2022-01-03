package com.nh.netty.server.service.impl;

import com.nh.netty.annotation.Remote;
import com.nh.netty.server.service.UserService;

@Remote
public class UserServiceImpl implements UserService {

    @Override
    public Object getUserByName(String userName) {
        System.out.println("userName==="+userName);
        return "服务器响应ok==================";
    }
}
