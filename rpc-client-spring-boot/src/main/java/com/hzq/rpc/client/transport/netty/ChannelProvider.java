package com.hzq.rpc.client.transport.netty;

import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Netty Channel 连接管理器（支持连接复用、失效自动清理）
 */
public class ChannelProvider {
    private static final Logger log = LoggerFactory.getLogger(ChannelProvider.class);
    private static final String KEY_FORMAT = "%s:%d";
    private static final int MAX_CAPACITY = 100;

    /**
     * 使用 LRU 策略的线程安全连接缓存
     */
    private final Map<String, Channel> channelCache = new ConcurrentHashMap<String, Channel>(32) {
        protected boolean removeEldestEntry(Map.Entry<String, Channel> eldest) {
            boolean needRemove = size() > MAX_CAPACITY;
            if (needRemove) {
                closeChannelSilently(eldest.getValue());
                log.info("Evict inactive channel: {}", eldest.getKey());
            }
            return needRemove;
        }
    };

    public Channel get(String hostname, Integer port) {
        String key = formatKey(hostname, port);
        return channelCache.computeIfPresent(key, (k, ch) ->
                (ch.isActive()) ? ch : removeAndClose(k, ch)
        );
    }

    public Channel get(InetSocketAddress address) {
        return get(address.getHostName(), address.getPort());
    }

    public void put(String hostname, Integer port, Channel channel) {
        String key = formatKey(hostname, port);
        channelCache.compute(key, (k, oldCh) -> {
            if (oldCh != null && oldCh != channel) {
                closeChannelSilently(oldCh);
                log.debug("Replace old channel: {}", k);
            }
            return (channel.isActive()) ? channel : null;
        });
    }

    public void put(InetSocketAddress address, Channel channel) {
        put(address.getHostName(), address.getPort(), channel);
    }

    private Channel removeAndClose(String key, Channel channel) {
        channelCache.remove(key);
        closeChannelSilently(channel);
        log.debug("Remove inactive channel: {}", key);
        return null;
    }

    private static void closeChannelSilently(Channel channel) {
        try {
            if (channel != null) {
                channel.close().sync();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Channel close failed: {}", e.getMessage());
        }
    }

    private static String formatKey(String hostname, Integer port) {
        if (StringUtils.isBlank(hostname) || port == null) {
            throw new IllegalArgumentException("Invalid hostname or port");
        }
        return String.format(KEY_FORMAT, hostname.trim(), port);
    }
}
