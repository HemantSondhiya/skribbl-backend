package com.hemant.skribbl.model;

import lombok.Data;

@Data
public class RoomSettings {
    private int maxPlayers = 8;
    private int rounds = 3;
    private int drawTimeSeconds = 80;
    private int wordChoices = 3;
    private boolean hintsEnabled = true;
    private boolean privateRoom = true;
}
