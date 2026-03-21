package com.hemant.skribbl.Service;

import com.hemant.skribbl.model.Player;
import com.hemant.skribbl.model.Room;
import com.hemant.skribbl.model.RoomSettings;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RoomService {
    Map<String, Object> createRoom(String hostName, RoomSettings settings);
    Map<String, Object> joinRoom(String roomCode, String playerName);
    Map<String, Object> joinRandomPublicRoom(String playerName);
    void setReady(String roomCode, String playerId, boolean ready);
    List<Room> listPublicLobbyRooms();
    Room getRoomOrThrow(String roomCode);
    Optional<Player> findPlayer(Room room, String playerId);
}
