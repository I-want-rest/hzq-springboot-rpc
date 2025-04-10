package com.hzq.rpc.core.serialization.hessian;

import com.caucho.hessian.io.HessianSerializerInput;
import com.caucho.hessian.io.HessianSerializerOutput;
import com.hzq.rpc.core.exception.SerializeException;
import com.hzq.rpc.core.serialization.Serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Hessian 序列化算法
 * @ClassName HessianSerialization
 */
public class HessianSerialization implements Serialization {
    @Override
    public <T> byte[] serialize(T object) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            HessianSerializerOutput hso = new HessianSerializerOutput(baos);
            hso.writeObject(object);
            hso.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new SerializeException("Hessian serialize failed.", e);
        }
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            HessianSerializerInput hsi = new HessianSerializerInput(bis);
            return (T) hsi.readObject();
        } catch (IOException e) {
            throw new SerializeException("Hessian deserialize failed.", e);
        }
    }

}
