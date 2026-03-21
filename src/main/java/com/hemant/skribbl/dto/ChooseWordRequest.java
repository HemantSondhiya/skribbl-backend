package com.hemant.skribbl.dto;



import lombok.Data;

@Data
public class ChooseWordRequest {
    private String roomCode;
    private String playerId;
    private String word;
}
