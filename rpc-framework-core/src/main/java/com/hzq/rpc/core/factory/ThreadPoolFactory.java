package com.hzq.rpc.core.factory;

import com.hzq.rpc.core.config.ThreadPoolConfig;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池工厂类
 *
 */
public class ThreadPoolFactory {

    private static final int AVAILABLE_PROCESSOR_NUMBER = Runtime.getRuntime().availableProcessors();

    private static final ThreadPoolConfig threadPoolConfig = new ThreadPoolConfig();

    public static ThreadPoolExecutor getDefaultThreadPool() {
        return new ThreadPoolExecutor(
                threadPoolConfig.getCorePoolSize(),
                threadPoolConfig.getMaximumPoolSize(),
                threadPoolConfig.getKeepAliveTime(),
                threadPoolConfig.getTimeUnit(),
                threadPoolConfig.getWorkQueue());
    }
    public static ThreadPoolExecutor getIOBoundThreadPool() {
        return new ThreadPoolExecutor(
               AVAILABLE_PROCESSOR_NUMBER * 2,
                AVAILABLE_PROCESSOR_NUMBER * 4,
                threadPoolConfig.getKeepAliveTime(),
                threadPoolConfig.getTimeUnit(),
                threadPoolConfig.getWorkQueue());
    }
    public static ThreadPoolExecutor getCPUBoundThreadPool() {
        return new ThreadPoolExecutor(
                AVAILABLE_PROCESSOR_NUMBER+1,
                AVAILABLE_PROCESSOR_NUMBER+1 ,
                threadPoolConfig.getKeepAliveTime(),
                threadPoolConfig.getTimeUnit(),
                threadPoolConfig.getWorkQueue());
    }

}
