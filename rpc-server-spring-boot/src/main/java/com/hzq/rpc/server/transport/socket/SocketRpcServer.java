package com.hzq.rpc.server.transport.socket;

import com.hzq.rpc.core.exception.RpcException;
import com.hzq.rpc.core.factory.ThreadPoolFactory;
import com.hzq.rpc.server.transport.RpcServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Socket 的 RpcServer 实现类
 * <p>
 * SocketServer 接受和发送的数据为：RpcRequest, RpcResponse
 * </p>
 *
 */
@Slf4j
public class SocketRpcServer implements RpcServer {


    // 网络传输io密集型，线程大小设置为：cpu * 2
    private final ThreadPoolExecutor threadPool = ThreadPoolFactory.getIOBoundThreadPool();

    @Override
    public void start(Integer port) {
        try (ServerSocket serverSocket = new ServerSocket()) {
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            serverSocket.bind(new InetSocketAddress(hostAddress, port));
            Socket socket;
            // 循环接受客户端 Socket 连接（accept为阻塞时等待连接）
            while ((socket = serverSocket.accept()) != null) {
                log.debug("The client connected [{}].", socket.getInetAddress());
                threadPool.execute(new SocketRpcRequestHandler(socket));
            }
            // 服务端连断开，关闭线程池
            threadPool.shutdown();
        } catch (IOException e) {
            throw new RpcException(String.format("The socket server failed to start on port %d.", port), e);
        }
    }
}
