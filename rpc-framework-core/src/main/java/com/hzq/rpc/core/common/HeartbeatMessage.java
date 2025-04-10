package com.hzq.rpc.core.common;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
/**
 *直接在Message中实现，未使用
 */
@Data
@Builder
public class HeartbeatMessage implements Serializable {
    private String msg;

}
