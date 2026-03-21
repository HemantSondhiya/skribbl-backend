package com.hemant.skribbl.dto;

import lombok.Data;

@Data
public class ChatMessageRequest {

    private String roomCode;
    private String senderId;
    private String playerId; // backward-compatible alias from older clients

    private String receiverId; // optional (for private)

    private String message;
    private String text; // backward-compatible alias from older clients
    private String type; // PUBLIC / PRIVATE

    public String effectivePlayerId() {
        return senderId != null && !senderId.isBlank() ? senderId : playerId;
    }

    public String effectiveMessage() {
        return message != null && !message.isBlank() ? message : text;
    }

    public String effectiveReceiverId() {
        return receiverId;
    }

    public boolean isPrivateMessage() {
        return "PRIVATE".equalsIgnoreCase(type) ||
                (receiverId != null && !receiverId.isBlank());
    }
}
