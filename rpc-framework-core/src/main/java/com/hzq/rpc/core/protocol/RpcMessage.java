package com.hzq.rpc.core.protocol;

import lombok.Data;

/**
 * Rpc 消息协议类
 *
 */
@Data
public class RpcMessage {

    /**
     * 请求头 - 协议信息
     */
    private MessageHeader header;

    /**
     * 消息体
     */
    private Object body;

}
