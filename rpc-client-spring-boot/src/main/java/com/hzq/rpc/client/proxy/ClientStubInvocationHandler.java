package com.hzq.rpc.client.proxy;

import com.hzq.rpc.client.config.RpcClientProperties;
import com.hzq.rpc.client.transport.RpcClient;
import com.hzq.rpc.core.discovery.ServiceDiscovery;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;


public class ClientStubInvocationHandler implements InvocationHandler {

    /**
     * 服务发现中心
     */
    private final ServiceDiscovery serviceDiscovery;

    /**
     * Rpc客户端
     */
    private final RpcClient rpcClient;

    /**
     * Rpc 客户端配置属性
     */
    private final RpcClientProperties properties;

    /**
     * 服务名称：接口-版本
     */
    private final String serviceName;


    public ClientStubInvocationHandler(ServiceDiscovery serviceDiscovery, RpcClient rpcClient, RpcClientProperties properties, String serviceName) {
        this.serviceDiscovery = serviceDiscovery;
        this.rpcClient = rpcClient;
        this.properties = properties;
        this.serviceName = serviceName;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 执行远程方法调用
        return RemoteMethodCall.remoteCall(serviceDiscovery, rpcClient, serviceName, properties, method, args);
    }
}
