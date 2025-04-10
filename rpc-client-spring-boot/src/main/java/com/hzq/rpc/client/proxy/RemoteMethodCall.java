package com.hzq.rpc.client.proxy;

import com.hzq.rpc.client.common.RequestMetadata;
import com.hzq.rpc.client.config.RpcClientProperties;
import com.hzq.rpc.client.transport.RpcClient;
import com.hzq.rpc.core.common.RpcRequest;
import com.hzq.rpc.core.common.RpcResponse;
import com.hzq.rpc.core.common.ServiceInfo;
import com.hzq.rpc.core.discovery.ServiceDiscovery;
import com.hzq.rpc.core.exception.RpcException;
import com.hzq.rpc.core.protocol.MessageHeader;
import com.hzq.rpc.core.protocol.RpcMessage;

import java.lang.reflect.Method;


public class RemoteMethodCall {

    /**
     * 发起 rpc 远程方法调用的公共方法
     *
     */
    public static Object remoteCall(ServiceDiscovery discovery, RpcClient rpcClient, String serviceName,
                                    RpcClientProperties properties, Method method, Object[] args) {
        // 构建请求头
        MessageHeader header = MessageHeader.build(properties.getSerialization());
        // 构建请求体
        RpcRequest request = new RpcRequest();
        request.setServiceName(serviceName);
        request.setMethod(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameterValues(args);

        // 进行服务发现
        ServiceInfo serviceInfo = discovery.discover(request);
        if (serviceInfo == null) {
            throw new RpcException(String.format("The service [%s] was not found in the remote registry center.",
                    serviceName));
        }

        // 构建通信协议信息
        RpcMessage rpcMessage = new RpcMessage();
        rpcMessage.setHeader(header);
        rpcMessage.setBody(request);

        // 构建请求元数据
        RequestMetadata metadata = RequestMetadata.builder()
                .rpcMessage(rpcMessage)
                .serverAddr(serviceInfo.getAddress())
                .port(serviceInfo.getPort())
                .timeout(properties.getTimeout()).build();
        // 发送网络请求，获取结果
        RpcMessage responseRpcMessage = rpcClient.sendRpcRequest(metadata);

        if (responseRpcMessage == null) {
            throw new RpcException("Remote procedure call timeout.");
        }

        // 获取响应结果
        RpcResponse response = (RpcResponse) responseRpcMessage.getBody();

        // 如果 远程调用 发生错误
        if (response.getExceptionValue() != null) {
            throw new RpcException(response.getExceptionValue());
        }
        // 返回响应结果
        return response.getReturnValue();
    }

}
