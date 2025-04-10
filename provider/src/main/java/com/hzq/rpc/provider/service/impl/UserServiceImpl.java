package com.hzq.rpc.provider.service.impl;

import com.hzq.rpc.api.pojo.User;
import com.hzq.rpc.api.service.UserService;
import com.hzq.rpc.server.annotation.RpcService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RpcService(interfaceClass = UserService.class)
public class UserServiceImpl implements UserService {

    @Override
    public User queryUser() {
        return new User("hzq", "123456", 25);
    }

    @Override
    public List<User> getAllUsers() {
        // 注意：直接使用 Arrays.ArrayList 会导致序列化异常
        return new ArrayList<>(Arrays.asList(new User("LiHua", "123456", 23),
                new User("hzq", "123456", 23),
                new User("hzq", "123456", 24)));
    }
}
