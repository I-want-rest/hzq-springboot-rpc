package com.hzq.rpc.client.transport;

import com.hzq.rpc.client.common.RequestMetadata;
import com.hzq.rpc.core.protocol.RpcMessage;

/**
 * Rpc 客户端类，负责向服务端发起请求（远程过程调用）
 *
 */
public interface RpcClient {

    /**
     * 发起远程过程调用
     *
     * @param requestMetadata rpc 请求元数据
     * @return 响应结果
     */
    RpcMessage sendRpcRequest(RequestMetadata requestMetadata);

}
