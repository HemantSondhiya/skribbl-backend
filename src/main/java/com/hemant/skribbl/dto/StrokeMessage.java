package com.hemant.skribbl.dto;

import lombok.Data;

@Data
public class StrokeMessage {
    private String roomCode;
    private String playerId;
    private String strokeId;
    private String color;
    private int size;
    private double x;
    private double y;
}
