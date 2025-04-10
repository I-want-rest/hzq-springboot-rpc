package com.hzq.rpc.client.spring;

import com.hzq.rpc.client.annotation.RpcReference;
import com.hzq.rpc.client.proxy.ClientStubProxyFactory;
import com.hzq.rpc.core.exception.RpcException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;

/**
 * 这是一个 Spring 的 BeanPostProcessor 实现类，用于在 Spring 容器初始化 Bean 后，扫描所有被 @RpcReference 注解标记的字段，并动态替换为 RPC 代理对象，从而实现依赖注入
 *
 * @see RpcReference
 */
public class RpcClientBeanPostProcessor implements BeanPostProcessor {

    private final ClientStubProxyFactory proxyFactory;

    public RpcClientBeanPostProcessor(ClientStubProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
    }
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 获取该 bean 的类的所有属性（getFields - 获取所有的public属性，getDeclaredFields - 获取所有声明的属性，不区分访问修饰符）
        Field[] fields = bean.getClass().getDeclaredFields();
        // 遍历所有属性
        for (Field field : fields) {
            // 判断是否被 RpcReference 注解标注
            if (field.isAnnotationPresent(RpcReference.class)) {
                RpcReference rpcReference = field.getAnnotation(RpcReference.class);
                // filed.class = java.lang.reflect.Field
                // filed.type = com.hzq.xxx.service.XxxService
                Class<?> clazz = field.getType();
                try {
                    //优先级: 若同时指定 interfaceClass 和 interfaceName，interfaceClass 优先级更高。
                    //动态代理：通过 proxyFactory 创建代理对象，替换原始字段值。
                    // 如果指定了全限定类型接口名
                    if (!"".equals(rpcReference.interfaceName())) {
                        clazz = Class.forName(rpcReference.interfaceName());
                    }
                    // 如果指定了接口类型
                    if (rpcReference.interfaceClass() != void.class) {
                        clazz = rpcReference.interfaceClass();
                    }
                    // 获取指定类型的代理对象
                    Object proxy = proxyFactory.getProxy(clazz, rpcReference.version());
                    // 关闭安全检查
                    field.setAccessible(true);
                    // 设置域的值为代理对象
                    field.set(bean, proxy);
                } catch (ClassNotFoundException | IllegalAccessException e) {
                    throw new RpcException(String.format("Failed to obtain proxy object, the type of field %s is %s, " +
                            "and the specified loaded proxy type is %s.", field.getName(), field.getClass(), clazz), e);
                }
            }
        }
        return bean;
    }

}
