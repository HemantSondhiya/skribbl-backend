package com.hemant.skribbl.Service;
import com.hemant.skribbl.dto.ChatMessageRequest;
import com.hemant.skribbl.model.ChatMessage;
import com.hemant.skribbl.model.Player;
import com.hemant.skribbl.model.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendPublicMessage(ChatMessageRequest request) {
        String roomCode = request.getRoomCode();
        String senderId = request.effectivePlayerId();
        String messageText = request.effectiveMessage();

        if (isBlank(roomCode) || isBlank(senderId) || isBlank(messageText)) {
            return;
        }

        Room room = roomService.getRoomOrThrow(roomCode);

        Player sender = roomService.findPlayer(room, senderId)
                .orElseThrow(() -> new NoSuchElementException("Sender not found"));

        ChatMessage message = new ChatMessage();
        message.setType(ChatMessage.Type.PUBLIC);
        message.setRoomCode(roomCode);
        message.setSenderId(sender.getId());
        message.setSenderName(sender.getName());
        message.setMessage(messageText);
        message.setTimestamp(System.currentTimeMillis());

        messagingTemplate.convertAndSend(
                "/topic/rooms/" + roomCode + "/chat",
                message
        );
    }

    @Override
    public void sendPrivateMessage(ChatMessageRequest request) {
        String roomCode = request.getRoomCode();
        String senderId = request.effectivePlayerId();
        String receiverId = request.effectiveReceiverId();
        String messageText = request.effectiveMessage();

        if (isBlank(roomCode) || isBlank(senderId) || isBlank(receiverId) || isBlank(messageText)) {
            return;
        }

        Room room = roomService.getRoomOrThrow(roomCode);

        Player sender = roomService.findPlayer(room, senderId)
                .orElseThrow(() -> new NoSuchElementException("Sender not found"));

        Player receiver = roomService.findPlayer(room, receiverId)
                .orElseThrow(() -> new NoSuchElementException("Receiver not found"));

        ChatMessage message = new ChatMessage();
        message.setType(ChatMessage.Type.PRIVATE);
        message.setRoomCode(roomCode);
        message.setSenderId(sender.getId());
        message.setSenderName(sender.getName());
        message.setReceiverId(receiver.getId());
        message.setMessage(messageText);
        message.setTimestamp(System.currentTimeMillis());

        messagingTemplate.convertAndSend("/topic/users/" + receiver.getId() + "/private", message);
        messagingTemplate.convertAndSend("/topic/users/" + sender.getId() + "/private", message);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
