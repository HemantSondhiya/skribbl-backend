package com.hemant.skribbl.controller;

import com.hemant.skribbl.Service.RoomService;
import com.hemant.skribbl.dto.CreateRoomRequest;
import com.hemant.skribbl.dto.JoinRandomRoomRequest;
import com.hemant.skribbl.dto.JoinRoomRequest;
import com.hemant.skribbl.dto.ReadyUpRequest;
import com.hemant.skribbl.dto.RoomResponse;
import com.hemant.skribbl.model.Room;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @PostMapping()
    public RoomResponse createRoom(@Valid @RequestBody CreateRoomRequest createRoomRequest) {
        Map<String, Object> result = roomService.createRoom(createRoomRequest.getHostName(), createRoomRequest.getSettings());
        return new RoomResponse((String) result.get("playerId"), (Room) result.get("room"));
    }

    @PostMapping("/{roomCode}/join")
    public RoomResponse joinRoom(@PathVariable String roomCode, @Valid @RequestBody JoinRoomRequest request) {
        Map<String, Object> result = roomService.joinRoom(roomCode, request.getPlayerName());
        return new RoomResponse((String) result.get("playerId"), (Room) result.get("room"));
    }

    @PostMapping("/join-random")
    public RoomResponse joinRandomRoom(@Valid @RequestBody JoinRandomRoomRequest request) {
        Map<String, Object> result = roomService.joinRandomPublicRoom(request.getPlayerName());
        return new RoomResponse((String) result.get("playerId"), (Room) result.get("room"));
    }

    @PostMapping("/{roomCode}/ready")
    public void setReady(@PathVariable String roomCode, @Valid @RequestBody ReadyUpRequest request) {
        roomService.setReady(roomCode, request.getPlayerId(), request.isReady());
    }

    @GetMapping("/public")
    public List<Room> getPublicRooms() {
        return roomService.listPublicLobbyRooms();
    }


    @GetMapping("/{roomCode}")
    public Room getRoom(@PathVariable String roomCode) {
        return roomService.getRoomOrThrow(roomCode);
    }


}
