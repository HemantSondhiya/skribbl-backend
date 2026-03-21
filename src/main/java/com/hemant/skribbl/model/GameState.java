package com.hemant.skribbl.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GameState {
    private GamePhase phase = GamePhase.LOBBY;
    private int currentRound = 0;
    private int drawerIndex = 0;
    private String currentDrawerId;
    private String currentWord;
    private String maskedWord;
    private int remainingSeconds;
    private List<String> currentWordOptions = new ArrayList<>();
}
