package com.hzq.rpc.client.spring;

import com.hzq.rpc.core.discovery.ServiceDiscovery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.lang.NonNull;

@Slf4j
public class RpcClientExitDisposableBean implements DisposableBean {

    private static final String SUCCESS_MSG = "RPC client resource release completed successfully.";
    private static final String FAILURE_MSG = "RPC client resource release failed.";

    private final ServiceDiscovery serviceDiscovery;

    public RpcClientExitDisposableBean(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    @Override
    public void destroy() {
        try {
            if (serviceDiscovery != null) {
                serviceDiscovery.destroy();
                log.info(SUCCESS_MSG);
            } else {
                log.warn("ServiceDiscovery instance is null, skip resource release.");
            }
        } catch (Exception e) {
            log.error(FAILURE_MSG, e);
            throw new IllegalStateException(FAILURE_MSG, e); // 可选：是否允许异常传播
        }
    }
}
