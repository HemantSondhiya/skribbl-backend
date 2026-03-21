package com.hemant.skribbl.controller;

import com.hemant.skribbl.Service.ChatService;
import com.hemant.skribbl.Service.GameService;
import com.hemant.skribbl.dto.ChatMessageRequest;
import com.hemant.skribbl.dto.ChooseWordRequest;
import com.hemant.skribbl.dto.StartGameRequest;
import com.hemant.skribbl.dto.StrokeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class GameSocketController {

        private final GameService gameService;
        private final ChatService chatService;

        @MessageMapping("/game.start")
        public void startGame(StartGameRequest request) {
            gameService.startGame(request.getRoomCode(), request.getPlayerId());
        }

        @MessageMapping("/game.choose-word")
        public void chooseWord(ChooseWordRequest request) {
            gameService.chooseWord(request.getRoomCode(), request.getPlayerId(), request.getWord());
        }

        @MessageMapping("/draw.add")
        public void addDrawPoint(StrokeMessage request) {
            gameService.addPointToStroke(
                    request.getRoomCode(),
                    request.getPlayerId(),
                    request.getStrokeId(),
                    request.getColor(),
                    request.getSize(),
                    request.getX(),
                    request.getY()
            );
        }

        @MessageMapping("/draw.end")
        public void endDraw(StrokeMessage request) {
            gameService.finishStroke(request.getRoomCode(), request.getStrokeId());
        }

        @MessageMapping("/draw.undo")
        public void undo(StrokeMessage request) {
            gameService.undo(request.getRoomCode(), request.getPlayerId());
        }

        @MessageMapping("/draw.clear")
        public void clear(StrokeMessage request) {
            gameService.clearCanvas(request.getRoomCode(), request.getPlayerId());
        }

    @MessageMapping("/chat.send")
    public void chat(ChatMessageRequest request) {
        if (request.isPrivateMessage()) {
            chatService.sendPrivateMessage(request);
            return;
        }

        gameService.handleChatOrGuess(
                request.getRoomCode(),
                request.effectivePlayerId(),
                request.effectiveMessage()
        );
    }
}


