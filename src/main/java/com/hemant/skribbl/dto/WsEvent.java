package com.hemant.skribbl.dto;
import lombok.Data;

// Generic WebSocket event wrapper — type + data
@Data
public class WsEvent {
    private String type;
    private Object data;

    public WsEvent(String type, Object data) {
        this.type = type;
        this.data = data;
    }
}
