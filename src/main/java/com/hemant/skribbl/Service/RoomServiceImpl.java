package com.hemant.skribbl.Service;

import com.hemant.skribbl.model.Player;
import com.hemant.skribbl.model.GamePhase;
import com.hemant.skribbl.model.GameState;
import com.hemant.skribbl.model.Room;
import com.hemant.skribbl.model.RoomSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    @Override
    public Map<String, Object> createRoom(String hostName, RoomSettings settings) {
        String roomCode = generateRoomCode();
        String hostId = UUID.randomUUID().toString();

        if (hostName == null || hostName.isBlank()) {
            throw new IllegalArgumentException("Host name is required");
        }

        if (settings == null) {
            settings = new RoomSettings();
        }

        Player host = new Player(hostId, hostName.trim(), 0, true, true, false, true);

        Room room = new Room();
        room.setRoomCode(roomCode);
        room.setSettings(settings);
        room.getPlayers().add(host);

        rooms.put(roomCode, room);
        broadcastState(room);

        return Map.of(
                "playerId", hostId,
                "room", room
        );
    }

    @Override
    public Map<String, Object> joinRoom(String roomCode, String playerName) {
        Room room = getRoomOrThrow(roomCode);

        if (playerName == null || playerName.isBlank()) {
            throw new IllegalArgumentException("Player name is required");
        }

        validateJoinableRoom(room);

        boolean nameExists = room.getPlayers().stream()
                .anyMatch(player -> player.getName().equalsIgnoreCase(playerName.trim()));

        if (nameExists) {
            throw new IllegalStateException("Player name already exists in the room");
        }

        String playerId = UUID.randomUUID().toString();
        Player player = new Player(playerId, playerName.trim(), 0, false, true, false, false);
        room.getPlayers().add(player);
        broadcastState(room);

        return Map.of(
                "playerId", playerId,
                "room", room
        );
    }

    @Override
    public Map<String, Object> joinRandomPublicRoom(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            throw new IllegalArgumentException("Player name is required");
        }

        List<Room> candidates = rooms.values().stream()
                .filter(this::isPublicJoinableLobby)
                .toList();

        if (candidates.isEmpty()) {
            throw new RoomNotFoundException("No public rooms available");
        }

        Room room = candidates.get(random.nextInt(candidates.size()));
        validateJoinableRoom(room);

        boolean nameExists = room.getPlayers().stream()
                .anyMatch(player -> player.getName().equalsIgnoreCase(playerName.trim()));

        if (nameExists) {
            throw new IllegalStateException("Player name already exists in the room");
        }

        String playerId = UUID.randomUUID().toString();
        Player player = new Player(playerId, playerName.trim(), 0, false, true, false, false);
        room.getPlayers().add(player);
        broadcastState(room);

        return Map.of(
                "playerId", playerId,
                "room", room
        );
    }

    @Override
    public void setReady(String roomCode, String playerId, boolean ready) {
        Room room = getRoomOrThrow(roomCode);
        Player player = findPlayer(room, playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found in room"));

        if (room.getGameState().getPhase() != GamePhase.LOBBY) {
            throw new IllegalStateException("Ready state can only be changed in lobby");
        }

        player.setReady(ready);
        broadcastState(room);
    }

    @Override
    public List<Room> listPublicLobbyRooms() {
        return rooms.values().stream()
                .filter(this::isPublicJoinableLobby)
                .map(this::sanitizeRoomForClients)
                .toList();
    }

    private void validateJoinableRoom(Room room) {
        if (room.getPlayers().size() >= room.getSettings().getMaxPlayers()) {
            throw new IllegalStateException("Room is full");
        }

        if (room.getGameState().getPhase() != GamePhase.LOBBY) {
            throw new IllegalStateException("Game already started");
        }
    }

    @Override
    public Room getRoomOrThrow(String roomCode) {
        Room room = rooms.get(roomCode);
        if (room == null) {
            throw new RoomNotFoundException("Room not found");
        }
        return room;
    }

    @Override
    public Optional<Player> findPlayer(Room room, String playerId) {
        return room.getPlayers().stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst();
    }

    private String generateRoomCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        String code;

        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            code = sb.toString();
        } while (rooms.containsKey(code));

        return code;
    }

    private boolean isPublicJoinableLobby(Room room) {
        return !room.getSettings().isPrivateRoom()
                && room.getGameState().getPhase() == GamePhase.LOBBY
                && room.getPlayers().size() < room.getSettings().getMaxPlayers();
    }

    private void broadcastState(Room room) {
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + room.getRoomCode() + "/state",
                sanitizeRoomForClients(room)
        );
    }

    private Room sanitizeRoomForClients(Room room) {
        Room copy = new Room();
        copy.setRoomCode(room.getRoomCode());
        copy.setSettings(room.getSettings());
        copy.setPlayers(new ArrayList<>(room.getPlayers()));
        copy.setStrokes(new ArrayList<>(room.getStrokes()));

        GameState source = room.getGameState();
        GameState safeState = new GameState();
        safeState.setPhase(source.getPhase());
        safeState.setCurrentRound(source.getCurrentRound());
        safeState.setDrawerIndex(source.getDrawerIndex());
        safeState.setCurrentDrawerId(source.getCurrentDrawerId());
        safeState.setCurrentWordOptions(Collections.emptyList());
        safeState.setRemainingSeconds(source.getRemainingSeconds());

        if (source.getPhase() == GamePhase.DRAWING) {
            safeState.setMaskedWord(source.getMaskedWord());
        }

        copy.setGameState(safeState);
        return copy;
    }
}
