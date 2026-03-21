package com.hemant.skribbl.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Room {
    private String roomCode;
    private RoomSettings settings;
    private List<Player> players = new ArrayList<>();
    private GameState gameState = new GameState();
    private List<Stroke> strokes = new ArrayList<>();
}
