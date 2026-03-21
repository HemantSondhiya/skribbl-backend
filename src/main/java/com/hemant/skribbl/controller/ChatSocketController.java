package com.hemant.skribbl.controller;

import com.hemant.skribbl.Service.ChatService;
import com.hemant.skribbl.dto.ChatMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final ChatService chatService;

    @MessageMapping("/chat.public")
    public void publicMessage(ChatMessageRequest request) {
        chatService.sendPublicMessage(request);
    }

    @MessageMapping("/chat.private")
    public void privateMessage(ChatMessageRequest request) {
        chatService.sendPrivateMessage(request);
    }
}
