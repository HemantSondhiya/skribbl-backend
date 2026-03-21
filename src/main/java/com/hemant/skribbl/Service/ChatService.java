package com.hemant.skribbl.Service;


import com.hemant.skribbl.dto.ChatMessageRequest;

public interface ChatService {
    void sendPublicMessage(ChatMessageRequest request);
    void sendPrivateMessage(ChatMessageRequest request);
}
