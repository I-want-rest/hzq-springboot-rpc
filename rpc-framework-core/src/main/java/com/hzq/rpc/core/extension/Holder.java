package com.hzq.rpc.core.extension;

/**
 * Holder 类，作用是为不可变的对象引用提供一个可变的包装
 *
 */
public class Holder<T> {

    private volatile T value;

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

}
