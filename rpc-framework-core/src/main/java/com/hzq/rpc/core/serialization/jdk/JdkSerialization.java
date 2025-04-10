package com.hzq.rpc.core.serialization.jdk;

import com.hzq.rpc.core.exception.SerializeException;
import com.hzq.rpc.core.serialization.Serialization;

import java.io.*;

/**
 * JDK 序列化算法
 */
public class JdkSerialization implements Serialization {
    @Override
    public <T> byte[] serialize(T object) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new SerializeException("Jdk serialize failed.", e);
        }
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new SerializeException("Jdk deserialize failed.", e);
        }
    }
}
