package com.hzq.rpc.client.transport.netty;

import com.hzq.rpc.client.common.RequestMetadata;
import com.hzq.rpc.client.transport.RpcClient;
import com.hzq.rpc.core.common.RpcRequest;
import com.hzq.rpc.core.enums.MessageType;
import com.hzq.rpc.core.enums.SerializationType;
import com.hzq.rpc.core.protocol.MessageHeader;
import com.hzq.rpc.core.protocol.RpcMessage;


public class TestNettyClient {

    public static void main(String[] args) {
        RpcClient rpcClient = new NettyRpcClient();
        RpcMessage rpcMessage = new RpcMessage();
        MessageHeader header = MessageHeader.build(SerializationType.KRYO.name());
        header.setMessageType(MessageType.REQUEST.getType());
        rpcMessage.setHeader(header);
        RpcRequest request = new RpcRequest();
        rpcMessage.setBody(request);
        RequestMetadata metadata = RequestMetadata.builder()
                .rpcMessage(rpcMessage)
                .serverAddr("192.168.23.3")
                .port(8881).build();
        rpcClient.sendRpcRequest(metadata);
    }

}
