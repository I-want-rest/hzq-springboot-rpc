package com.hzq.rpc.core.extension.factory;

import com.hzq.rpc.core.extension.ExtensionFactory;
import com.hzq.rpc.core.extension.ExtensionLoader;
import com.hzq.rpc.core.extension.SPI;

public class SpiExtensionFactory implements ExtensionFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getExtension(Class<?> type, String name) {
        // 1. 校验是否为 SPI 扩展接口
        if (!isSpiExtensionInterface(type)) {
            return null;
        }

        // 2. 获取扩展加载器
        ExtensionLoader<?> extensionLoader = ExtensionLoader.getExtensionLoader(type);

        // 3. 根据名称获取扩展实例
        try {
            return (T) extensionLoader.getExtension(name);
        } catch (IllegalArgumentException e) {
            // 处理未找到扩展实现的情况
            throw new ExtensionNotFoundException("Extension not found for name: " + name, e);
        }
    }

    /**
     * 校验类型是否为 SPI 扩展接口
     */
    private boolean isSpiExtensionInterface(Class<?> type) {
        return type.isInterface() && type.isAnnotationPresent(SPI.class);
    }
}

// 自定义异常类
class ExtensionNotFoundException extends RuntimeException {
    public ExtensionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
