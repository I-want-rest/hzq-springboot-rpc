package com.hzq.rpc.api.service;

import com.hzq.rpc.api.pojo.User;

import java.util.List;

public interface UserService {

    User queryUser();

    List<User> getAllUsers();

}
