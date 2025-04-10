package com.hzq.rpc.core.codec;

import com.hzq.rpc.core.common.RpcRequest;
import com.hzq.rpc.core.common.RpcResponse;
import com.hzq.rpc.core.constant.ProtocolConstants;
import com.hzq.rpc.core.enums.MessageType;
import com.hzq.rpc.core.enums.SerializationType;
import com.hzq.rpc.core.protocol.MessageHeader;
import com.hzq.rpc.core.protocol.RpcMessage;
import com.hzq.rpc.core.serialization.Serialization;
import com.hzq.rpc.core.serialization.SerializationFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CodecException;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Sharable
public class SharableRpcMessageCodec extends MessageToMessageCodec<ByteBuf, RpcMessage> {
    // 协议常量
    private static final int MAGIC_NUM_LENGTH = ProtocolConstants.MAGIC_NUM.length;
    private static final int HEADER_FIXED_LENGTH = MAGIC_NUM_LENGTH + 1 + 1 + 1 + 1 + 4 + 4; // 魔数4B + 版本1B + 序列化类型1B + 消息类型1B + 状态1B + 序列号4B + 长度4B
    private static final SerializationType DEFAULT_SERIALIZER = SerializationType.HESSIAN;

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage msg, List<Object> out) {
        try {
            ByteBuf buf = ctx.alloc().buffer();
            writeHeader(buf, msg.getHeader());
            serializeBody(buf, msg);
            out.add(buf);
        } catch (Exception e) {
            log.error("Encode failed | MsgType:{} | Cause:{}",
                    msg.getHeader().getMessageType(), e.getMessage());
            throw new CodecException("Encode error", e);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        try {
            validateMagicNumber(in);
            MessageHeader header = readHeader(in);
            validatePayloadLength(in, header.getLength());
            RpcMessage msg = deserializeBody(header, in);
            out.add(msg);
        } catch (Exception e) {
            log.error("Decode failed | Cause:{}", e.getMessage());
            throw new CodecException("Decode error", e);
        }
    }

    // ---------------------- 编码器工具方法 ----------------------
    private void writeHeader(ByteBuf buf, MessageHeader header) {
        buf.writeBytes(ProtocolConstants.MAGIC_NUM)
                .writeByte(header.getVersion())
                .writeByte(header.getSerializerType())
                .writeByte(header.getMessageType())
                .writeByte(header.getMessageStatus())
                .writeInt(header.getSequenceId());
    }

    private void serializeBody(ByteBuf buf, RpcMessage msg) {
        MessageHeader header = msg.getHeader();
        // 获取序列化器（兼容旧版本未设置序列化类型的情况）
        SerializationType serializerType = Optional.ofNullable(SerializationType.parseByType(header.getSerializerType()))
                .orElse(DEFAULT_SERIALIZER);
        Serialization serializer = SerializationFactory.getSerialization(serializerType);

        byte[] bodyBytes = serializer.serialize(msg.getBody());
        header.setLength(bodyBytes.length);

        buf.writeInt(header.getLength())
                .writeBytes(bodyBytes);
    }

    // ---------------------- 解码器工具方法 ----------------------
    private void validateMagicNumber(ByteBuf in) {
        byte[] magicNum = new byte[MAGIC_NUM_LENGTH];
        in.readBytes(magicNum);
        if (!Arrays.equals(magicNum, ProtocolConstants.MAGIC_NUM)) {
            throw new CodecException("Invalid magic number: " + Arrays.toString(magicNum));
        }
    }

    private MessageHeader readHeader(ByteBuf in) {
        return MessageHeader.builder()
                .magicNum(ProtocolConstants.MAGIC_NUM)
                .version(in.readByte())
                .serializerType(in.readByte())
                .messageType(in.readByte())
                .messageStatus(in.readByte())
                .sequenceId(in.readInt())
                .length(in.readInt())
                .build();
    }

    private void validatePayloadLength(ByteBuf in, int expectedLength) {
        if (in.readableBytes() < expectedLength) {
            throw new CodecException("Insufficient payload length. Expected:" + expectedLength
                    + ", Actual:" + in.readableBytes());
        }
    }

    private RpcMessage deserializeBody(MessageHeader header, ByteBuf in) {
        // 1. 获取序列化器
        SerializationType serializerType = Optional.ofNullable(SerializationType.parseByType(header.getSerializerType()))
                .orElse(DEFAULT_SERIALIZER);
        Serialization serializer = SerializationFactory.getSerialization(serializerType);

        // 2. 读取消息体
        byte[] bodyBytes = new byte[header.getLength()];
        in.readBytes(bodyBytes);

        // 3. 根据消息类型反序列化
        MessageType msgType = Optional.ofNullable(MessageType.parseByType(header.getMessageType()))
                .orElseThrow(() -> new CodecException("Unknown message type: " + header.getMessageType()));

        Object body;
        switch (msgType) {
            case REQUEST:
                body = serializer.deserialize(RpcRequest.class, bodyBytes);
                break;
            case RESPONSE:
                body = serializer.deserialize(RpcResponse.class, bodyBytes);
                break;
            case HEARTBEAT_REQUEST:
            case HEARTBEAT_RESPONSE:
                body = serializer.deserialize(String.class, bodyBytes);
                break;
            default:
                throw new CodecException("Unsupported message type: " + msgType);
        }
        RpcMessage msg = new RpcMessage();
        msg.setHeader(header);
        msg.setBody(body);
        return msg;
    }
}
