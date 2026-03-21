package com.hemant.skribbl.model;
import lombok.Data;

@Data
public class ChatMessage {

    public enum Type {
        PUBLIC,
        PRIVATE,
        SYSTEM
    }

    private Type type;
    private String roomCode;

    private String senderId;
    private String senderName;

    private String receiverId; // only for private

    private String message;
    private long timestamp;
}