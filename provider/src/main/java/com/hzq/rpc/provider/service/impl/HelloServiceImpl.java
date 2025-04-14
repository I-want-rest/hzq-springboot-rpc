package com.hzq.rpc.provider.service.impl;

import com.hzq.rpc.api.service.HelloService;
import com.hzq.rpc.server.annotation.RpcService;

@RpcService(interfaceClass = HelloService.class)
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHelloWorld() {
        return "Hello World";
    }

    @Override
    public String sayHello(String name) {
        return "Hello, " + name;
    }
}
