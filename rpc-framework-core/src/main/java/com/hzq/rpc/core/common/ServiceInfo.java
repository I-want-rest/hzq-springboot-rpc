package com.hzq.rpc.core.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceInfo implements Serializable {

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 服务名称：服务名-版本号
     */
    private String serviceName;

    /**
     * 版本号
     */
    private String version;

    /**
     * 服务提供方主机地址
     */
    private String address;

    /**
     * 服务提供方端口号
     */
    private Integer port;
}
