package com.hzq.rpc.server.spring;

import com.hzq.rpc.core.common.ServiceInfo;
import com.hzq.rpc.core.exception.RpcException;
import com.hzq.rpc.core.registry.ServiceRegistry;
import com.hzq.rpc.core.util.ServiceUtil;
import com.hzq.rpc.server.annotation.RpcService;
import com.hzq.rpc.server.config.RpcServerProperties;
import com.hzq.rpc.server.store.LocalServiceCache;
import com.hzq.rpc.server.transport.RpcServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class RpcServerBeanPostProcessor implements BeanPostProcessor, CommandLineRunner {

    private final ServiceRegistry serviceRegistry;
    private final RpcServer rpcServer;
    private final RpcServerProperties properties;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 处理代理类，获取原始类
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        // 检查注解是否存在
        if (targetClass.isAnnotationPresent(RpcService.class)) {
            RpcService rpcService = targetClass.getAnnotation(RpcService.class);
            processRpcService(bean, rpcService);
        }
        return bean;
    }

    /**
     * 处理 @RpcService 注解的 Bean
     */
    private void processRpcService(Object bean, RpcService rpcService) {
        try {
            String interfaceName = resolveInterfaceName(rpcService);
            String version = Optional.ofNullable(rpcService.version()).orElse("1.0");
            String serviceKey = ServiceUtil.serviceKey(interfaceName, version);

            ServiceInfo serviceInfo = buildServiceInfo(serviceKey, version);
            registerService(serviceInfo, serviceKey, bean);
        } catch (Exception e) {
            log.error("Failed to process RPC service: {}", bean.getClass().getName(), e);
            throw new RpcException("RPC service registration failed", e);
        }
    }

    /**
     * 解析服务接口名
     */
    private String resolveInterfaceName(RpcService rpcService) {
        String interfaceName = rpcService.interfaceName();
        if (StringUtils.hasText(interfaceName)) {
            return interfaceName;
        }
        Class<?> interfaceClass = rpcService.interfaceClass();
        Assert.isTrue(interfaceClass != void.class, "@RpcService must specify interfaceClass or interfaceName");
        return interfaceClass.getName();
    }

    /**
     * 构建服务信息
     */
    private ServiceInfo buildServiceInfo(String serviceKey, String version) {
        return ServiceInfo.builder()
                .appName(properties.getAppName())
                .serviceName(serviceKey)
                .version(version)
                .address(properties.getAddress())
                .port(properties.getPort())
                .build();
    }

    /**
     * 注册服务到注册中心并缓存本地
     */
    private void registerService(ServiceInfo serviceInfo, String serviceKey, Object bean) throws Exception {
        log.info("Registering RPC service: {} with info: {}", serviceKey, serviceInfo);
        serviceRegistry.register(serviceInfo);
        LocalServiceCache.addService(serviceKey, bean);
        log.debug("Successfully registered RPC service: {}", serviceKey);
    }

    @Override
    public void run(String... args) {
        startRpcServer();
        registerShutdownHook();
    }

    /**
     * 启动 RPC 服务器
     */
    private void startRpcServer() {
        new Thread(() -> {
            try {
                rpcServer.start(properties.getPort());
                log.info("RPC server started on port: {}", properties.getPort());
            } catch (Exception e) {
                log.error("Failed to start RPC server on port: {}", properties.getPort(), e);
                throw new RpcException("RPC server startup failed", e);
            }
        }, "RPC-Server-Thread").start();
    }

    /**
     * 注册 JVM 关闭钩子
     */
    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                log.info("Shutting down RPC services...");
                serviceRegistry.destroy();
                log.info("RPC services shutdown completed");
            } catch (Exception e) {
                log.error("Error during RPC service shutdown", e);
            }
        }, "Shutdown-Hook-Thread"));
    }
}
