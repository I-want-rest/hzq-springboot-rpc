package com.hzq.rpc.provider.service.impl;

import com.hzq.rpc.api.service.AbstractService;
import com.hzq.rpc.server.annotation.RpcService;


@RpcService(interfaceClass = AbstractService.class)
public class AbstractServiceImpl extends AbstractService {
    @Override
    public String abstractHello(String name) {
        return "abstract hello " + name;
    }
}
