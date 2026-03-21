package com.hemant.skribbl.Service;

public interface GameService {
    void startGame(String roomCode, String playerId);
    void chooseWord(String roomCode, String playerId, String word);
    void addPointToStroke(String roomCode, String playerId, String strokeId, String color, int size, double x, double y);
    void finishStroke(String roomCode, String strokeId);
    void undo(String roomCode, String playerId);
    void clearCanvas(String roomCode, String playerId);
    void handleChatOrGuess(String roomCode, String playerId, String text);
    void endRoundByTimeout(String roomCode);
}