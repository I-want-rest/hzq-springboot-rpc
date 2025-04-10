package com.hzq.rpc.client.handler;

import com.hzq.rpc.core.common.RpcResponse;
import com.hzq.rpc.core.constant.ProtocolConstants;
import com.hzq.rpc.core.enums.MessageType;
import com.hzq.rpc.core.enums.SerializationType;
import com.hzq.rpc.core.protocol.MessageHeader;
import com.hzq.rpc.core.protocol.RpcMessage;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcMessage> {

    public static final Map<Integer, Promise<RpcMessage>> UNPROCESSED_RPC_RESPONSES = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) {
        try {
            final MessageType messageType = MessageType.parseByType(msg.getHeader().getMessageType());
            switch (messageType) {
                case RESPONSE:
                    handleRpcResponse(msg);
                    break;
                case HEARTBEAT_RESPONSE:
                    log.trace("Received heartbeat response: {}", msg.getBody());
                    break;
                default:
                    log.warn("Unsupported message type: {}", messageType);
            }
        } finally {
            // Netty 会自动释放资源，此处可移除
            // ReferenceCountUtil.release(msg);
        }
    }

    private void handleRpcResponse(RpcMessage msg) {
        final int sequenceId = msg.getHeader().getSequenceId();
        final Promise<RpcMessage> promise = UNPROCESSED_RPC_RESPONSES.remove(sequenceId);

        if (promise == null) {
            log.warn("Received response for unknown sequence ID: {}", sequenceId);
            return;
        }

        final RpcResponse response = (RpcResponse) msg.getBody();
        if (response.getExceptionValue()!= null) {
            promise.tryFailure(response.getExceptionValue());
        } else {
            promise.trySuccess(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent && ((IdleStateEvent) evt).state() == IdleState.WRITER_IDLE) {
            log.warn("Write idle detected, sending heartbeat to {}", ctx.channel().remoteAddress());
            ctx.writeAndFlush(buildHeartbeatMessage())
                    .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }

    private RpcMessage buildHeartbeatMessage() {
        final MessageHeader header = MessageHeader.build(SerializationType.KRYO.name());
        header.setMessageType(MessageType.HEARTBEAT_REQUEST.getType());
        RpcMessage rpcMessage = new RpcMessage();
        rpcMessage.setHeader(header);
        rpcMessage.setBody(ProtocolConstants.PING);
        return rpcMessage;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Client exception caught: {}", cause.getMessage(), cause);
        ctx.close();
    }
}
