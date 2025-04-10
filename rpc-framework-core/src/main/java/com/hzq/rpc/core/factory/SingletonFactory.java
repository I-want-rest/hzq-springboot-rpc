package com.hzq.rpc.core.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 获取单实例对象的工厂
 *
 */
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 线程安全的单例工厂类
 */
public final class SingletonFactory {
    private static final Map<Class<?>, Object> INSTANCE_CACHE = new ConcurrentHashMap<>();

    private SingletonFactory() {
        throw new AssertionError("No instances allowed");
    }

    /**
     * 获取单例实例（线程安全）
     */
    public static <T> T getInstance(Class<T> clazz) {
        return clazz.cast(INSTANCE_CACHE.computeIfAbsent(clazz, SingletonFactory::createSingleton));
    }

    /**
     * 反射创建实例，封装异常信息
     */
    private static <T> T createSingleton(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Class " + clazz + " lacks public no-arg constructor", e);
        } catch (InstantiationException e) {
            throw new IllegalStateException("Failed to instantiate " + clazz + " (abstract class?)", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot access constructor of " + clazz, e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Constructor threw exception for " + clazz, e.getTargetException());
        }
    }

    /**
     * 单元测试辅助方法：清空缓存
     */
    static void clearCache() {
        INSTANCE_CACHE.clear();
    }
}

