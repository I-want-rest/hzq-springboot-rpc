package com.hzq.rpc.consumer.controller;

import com.hzq.rpc.api.pojo.User;
import com.hzq.rpc.api.service.UserService;
import com.hzq.rpc.client.annotation.RpcReference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class UserController {

    @RpcReference
    private UserService userService;


    @RequestMapping("/user/getUser")
    public User getUser() {

        return userService.queryUser();
    }

    @RequestMapping("/user/getAllUser")
    public List<User> getAllUser() {

        return userService.getAllUsers();
    }

}
