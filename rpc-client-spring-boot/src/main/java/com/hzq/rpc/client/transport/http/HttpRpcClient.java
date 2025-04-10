package com.hzq.rpc.client.transport.http;

import com.hzq.rpc.client.common.RequestMetadata;
import com.hzq.rpc.client.transport.RpcClient;
import com.hzq.rpc.core.common.RpcResponse;
import com.hzq.rpc.core.enums.SerializationType;
import com.hzq.rpc.core.exception.RpcException;
import com.hzq.rpc.core.protocol.RpcMessage;
import com.hzq.rpc.core.serialization.Serialization;
import com.hzq.rpc.core.serialization.SerializationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class HttpRpcClient implements RpcClient {
    private static final Logger log = LoggerFactory.getLogger(HttpRpcClient.class);
    private static final String CONTENT_TYPE = "application/rpc-msg";
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_INTERVAL_MS = 1000;

    @Override
    public RpcMessage sendRpcRequest(RequestMetadata requestMetadata) {
        int retryCount = 0;
        while (retryCount <= MAX_RETRIES) {
            HttpURLConnection connection = null;
            try {
                // 1. 构建请求URL
                URL url = new URL("http",
                        requestMetadata.getServerAddr(),
                        requestMetadata.getPort(),
                        "/" + requestMetadata.getServerAddr());

                // 2. 创建并配置连接
                connection = (HttpURLConnection) url.openConnection();
                configureConnection(connection, requestMetadata.getTimeout());

                // 3. 序列化请求体
                byte[] requestBody = serializeRequest(requestMetadata);

                // 4. 发送请求
                sendRequest(connection, requestBody);

                // 5. 处理响应
                return handleResponse(connection);
            } catch (SocketTimeoutException e) {
                log.warn("RPC request timeout, retry {}/{}", retryCount, MAX_RETRIES);
                if (retryCount++ >= MAX_RETRIES) {
                    throw new RpcException("RPC call timeout after retries", e);
                }
                sleepRetryInterval();
            } catch (IOException e) {
                throw new RpcException("RPC communication failed", e);
            } catch (Exception e) {
                throw new RpcException("Unexpected RPC error", e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        throw new RpcException("Max retries exceeded");
    }

    private void configureConnection(HttpURLConnection conn, int timeout) throws ProtocolException {
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", CONTENT_TYPE);
        conn.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(timeout));
        conn.setReadTimeout((int) TimeUnit.SECONDS.toMillis(timeout));
    }

    private byte[] serializeRequest(RequestMetadata metadata) {
        Serialization serializer = SerializationFactory.getSerialization(SerializationType.parseByType(metadata.getRpcMessage().getHeader().getSerializerType()));
        return serializer.serialize(metadata.getRpcMessage().getBody());
    }

    private void sendRequest(HttpURLConnection conn, byte[] body) throws IOException {
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body);
            os.flush();
        }
    }

    private RpcMessage handleResponse(HttpURLConnection conn) throws IOException {
        int status = conn.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            throw new RpcException("Server responded with status: " + status);
        }

        try (InputStream is = conn.getInputStream();
             ObjectInputStream ois = new ObjectInputStream(is)) {
            RpcMessage message = new RpcMessage();
            message.setBody(ois.readObject());
            return message;
        } catch (ClassNotFoundException e) {
            throw new RpcException("Deserialization failed", e);
        }
    }

    private void sleepRetryInterval() {
        try {
            Thread.sleep(RETRY_INTERVAL_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
