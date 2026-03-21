package com.hemant.skribbl.dto;

import lombok.Data;

@Data
public class StartGameRequest {
    private String roomCode;
    private String playerId;
}
