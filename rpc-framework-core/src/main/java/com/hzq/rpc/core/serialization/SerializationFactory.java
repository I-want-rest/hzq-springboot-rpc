package com.hzq.rpc.core.serialization;

import com.hzq.rpc.core.enums.SerializationType;
import com.hzq.rpc.core.serialization.hessian.HessianSerialization;
import com.hzq.rpc.core.serialization.jdk.JdkSerialization;
import com.hzq.rpc.core.serialization.json.JsonSerialization;
import com.hzq.rpc.core.serialization.kryo.KryoSerialization;
import com.hzq.rpc.core.serialization.protostuff.ProtostuffSerialization;

/**
 * 序列化算法工厂，通过序列化枚举类型获取相应的序列化算法实例
 *


 * @ClassName SerializationFactory

 */
public class SerializationFactory {

    public static Serialization getSerialization(SerializationType enumType) {
        switch (enumType) {
            case JDK:
                return new JdkSerialization();
            case JSON:
                return new JsonSerialization();
            case HESSIAN:
                return new HessianSerialization();
            case KRYO:
                return new KryoSerialization();
            case PROTOSTUFF:
                return new ProtostuffSerialization();
            default:
                throw new IllegalArgumentException(String.format("The serialization type %s is illegal.",
                        enumType.name()));
        }
    }

}
