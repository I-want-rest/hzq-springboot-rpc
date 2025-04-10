package com.hzq.rpc.server.transport.netty;

import com.hzq.rpc.server.transport.RpcServer;


public class TestNettyServer {

    public static void main(String[] args) {
        RpcServer rpcServer = new NettyRpcServer();
        rpcServer.start(8880);
    }

}
