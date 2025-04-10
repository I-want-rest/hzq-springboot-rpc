package com.hzq.rpc.server.transport.http;

import com.hzq.rpc.core.factory.SingletonFactory;
import com.hzq.rpc.core.factory.ThreadPoolFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 */
public class DispatcherServlet extends HttpServlet {


    // 创建线程池
    private static ThreadPoolFactory threadPoolFactory=new ThreadPoolFactory();
    private static final ThreadPoolExecutor threadPool = threadPoolFactory.getIOBoundThreadPool();
//    private final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(cpuNum * 2, cpuNum * 2, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10000));


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpRpcRequestHandler handler = SingletonFactory.getInstance(HttpRpcRequestHandler.class);
        threadPool.submit(new Thread(() -> handler.handle(req, resp)));
    }
}
