package com.hemant.skribbl.Service;

import com.hemant.skribbl.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final RoomService roomService;
    private final WordService wordService;
    private final SimpMessagingTemplate messagingTemplate;

    private final Map<String, Stroke> activeStrokes = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> roomTimers = new ConcurrentHashMap<>();
    private final Map<String, Boolean> roundEnding = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    @Override
    public void startGame(String roomCode, String playerId) {
        Room room = roomService.getRoomOrThrow(roomCode);
        Player host = roomService.findPlayer(room, playerId)
                .orElseThrow(() -> new NoSuchElementException("Player not found"));

        if (!host.isHost()) {
            throw new IllegalStateException("Only host can start game");
        }

        if (room.getPlayers().size() < 2) {
            throw new IllegalStateException("At least 2 players are required to start");
        }

        boolean allNonHostReady = room.getPlayers().stream()
                .filter(player -> !player.isHost())
                .allMatch(Player::isReady);
        if (!allNonHostReady) {
            throw new IllegalStateException("All non-host players must be ready");
        }

        room.getPlayers().forEach(player -> {
            player.setScore(0);
            player.setGuessedCorrectly(false);
            player.setReady(false);
        });

        GameState state = room.getGameState();
        state.setCurrentRound(1);
        state.setDrawerIndex(0);
        state.setCurrentWord(null);
        state.setMaskedWord(null);
        state.setRemainingSeconds(0);

        startNextTurn(room);
    }

    @Override
    public void chooseWord(String roomCode, String playerId, String word) {
        if (isBlank(roomCode) || isBlank(playerId) || isBlank(word)) {
            throw new IllegalArgumentException("roomCode, playerId and word are required");
        }

        Room room = roomService.getRoomOrThrow(roomCode);
        GameState state = room.getGameState();

        if (!Objects.equals(state.getCurrentDrawerId(), playerId)) {
            throw new IllegalStateException("Only current drawer can choose word");
        }

        String selectedWord = resolveSelectedWord(state.getCurrentWordOptions(), word);
        if (selectedWord == null) {
            throw new IllegalStateException("Invalid word choice");
        }

        state.setCurrentWord(selectedWord);
        state.setMaskedWord(maskWord(selectedWord));
        state.setPhase(GamePhase.DRAWING);
        state.setRemainingSeconds(room.getSettings().getDrawTimeSeconds());

        room.getPlayers().forEach(p -> p.setGuessedCorrectly(false));
        room.getStrokes().clear();
        activeStrokes.clear();
        roundEnding.put(roomCode, false);

        broadcastState(room);
        startRoundTimer(room);
    }

    @Override
    public void addPointToStroke(String roomCode, String playerId, String strokeId,
                                 String color, int size, double x, double y) {
        if (isBlank(roomCode) || isBlank(playerId) || isBlank(strokeId)) {
            return;
        }

        Room room = roomService.getRoomOrThrow(roomCode);
        GameState state = room.getGameState();

        if (!Objects.equals(state.getCurrentDrawerId(), playerId)) {
            throw new IllegalStateException("Only drawer can draw");
        }

        if (state.getPhase() != GamePhase.DRAWING) {
            throw new IllegalStateException("Drawing is not active");
        }

        Stroke stroke = activeStrokes.computeIfAbsent(strokeId, id -> {
            Stroke s = new Stroke();
            s.setStrokeId(id);
            s.setPlayerId(playerId);
            s.setColor(color);
            s.setSize(size);
            return s;
        });

        Stroke.Point point = new Stroke.Point();
        point.setX(x);
        point.setY(y);
        stroke.getPoints().add(point);

        messagingTemplate.convertAndSend("/topic/rooms/" + roomCode + "/drawing/stroke", stroke);
    }

    @Override
    public void finishStroke(String roomCode, String strokeId) {
        if (isBlank(roomCode) || isBlank(strokeId)) {
            return;
        }

        Room room = roomService.getRoomOrThrow(roomCode);
        Stroke stroke = activeStrokes.remove(strokeId);

        if (stroke != null) {
            room.getStrokes().add(stroke);
            messagingTemplate.convertAndSend("/topic/rooms/" + roomCode + "/drawing", room.getStrokes());
        }
    }

    @Override
    public void undo(String roomCode, String playerId) {
        Room room = roomService.getRoomOrThrow(roomCode);

        if (!Objects.equals(room.getGameState().getCurrentDrawerId(), playerId)) {
            throw new IllegalStateException("Only drawer can undo");
        }

        if (!room.getStrokes().isEmpty()) {
            room.getStrokes().remove(room.getStrokes().size() - 1);
        }

        messagingTemplate.convertAndSend("/topic/rooms/" + roomCode + "/drawing", room.getStrokes());
    }

    @Override
    public void clearCanvas(String roomCode, String playerId) {
        Room room = roomService.getRoomOrThrow(roomCode);

        if (!Objects.equals(room.getGameState().getCurrentDrawerId(), playerId)) {
            throw new IllegalStateException("Only drawer can clear canvas");
        }

        room.getStrokes().clear();
        activeStrokes.clear();

        messagingTemplate.convertAndSend("/topic/rooms/" + roomCode + "/drawing", room.getStrokes());
    }

    @Override
    public void handleChatOrGuess(String roomCode, String playerId, String text) {
        if (isBlank(roomCode) || isBlank(playerId) || isBlank(text)) {
            return;
        }

        Room room = roomService.getRoomOrThrow(roomCode);
        Player sender = roomService.findPlayer(room, playerId).orElse(null);
        if (sender == null) {
            return;
        }

        GameState state = room.getGameState();

        boolean isCorrectGuess =
                state.getPhase() == GamePhase.DRAWING &&
                        !Objects.equals(state.getCurrentDrawerId(), playerId) &&
                        state.getCurrentWord() != null &&
                        normalize(text).equals(normalize(state.getCurrentWord())) &&
                        !sender.isGuessedCorrectly();

        if (isCorrectGuess) {
            sender.setGuessedCorrectly(true);

            int guesserPoints = calculateGuesserPoints(state.getRemainingSeconds());
            sender.setScore(sender.getScore() + guesserPoints);

            Player drawer = roomService.findPlayer(room, state.getCurrentDrawerId()).orElse(null);
            if (drawer != null) {
                drawer.setScore(drawer.getScore() + 25);
            }

            messagingTemplate.convertAndSend("/topic/rooms/" + roomCode + "/guess-result",
                    Map.of(
                            "correct", true,
                            "playerId", sender.getId(),
                            "playerName", sender.getName(),
                            "points", guesserPoints
                    )
            );

            messagingTemplate.convertAndSend("/topic/rooms/" + roomCode + "/chat",
                    Map.of(
                            "playerId", "system",
                            "playerName", "SYSTEM",
                            "text", sender.getName() + " guessed the word!",
                            "type", "SYSTEM"
                    )
            );

            broadcastState(room);

            boolean allGuessersDone = room.getPlayers().stream()
                    .filter(p -> !p.getId().equals(state.getCurrentDrawerId()))
                    .allMatch(Player::isGuessedCorrectly);

            if (allGuessersDone) {
                endRound(room);
            }
            return;
        }

        messagingTemplate.convertAndSend("/topic/rooms/" + roomCode + "/chat",
                Map.of(
                        "playerId", sender.getId(),
                        "playerName", sender.getName(),
                        "text", text
                )
        );
    }

    @Override
    public void endRoundByTimeout(String roomCode) {
        Room room = roomService.getRoomOrThrow(roomCode);
        endRound(room);
    }

    private void startRoundTimer(Room room) {
        String roomCode = room.getRoomCode();
        cancelRoomTimer(roomCode);

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                tick(roomCode);
            } catch (Exception ignored) {
            }
        }, 1, 1, TimeUnit.SECONDS);

        roomTimers.put(roomCode, future);
    }

    private void tick(String roomCode) {
        Room room = roomService.getRoomOrThrow(roomCode);
        GameState state = room.getGameState();

        if (state.getPhase() != GamePhase.DRAWING) {
            return;
        }

        int remaining = state.getRemainingSeconds() - 1;
        state.setRemainingSeconds(Math.max(remaining, 0));

        if (room.getSettings().isHintsEnabled()) {
            revealHintIfNeeded(room);
        }

        broadcastState(room);

        if (state.getRemainingSeconds() <= 0) {
            endRound(room);
        }
    }

    private void revealHintIfNeeded(Room room) {
        GameState state = room.getGameState();
        String word = state.getCurrentWord();

        if (word == null || word.isBlank()) {
            return;
        }

        int total = room.getSettings().getDrawTimeSeconds();
        int remaining = state.getRemainingSeconds();

        if (remaining == total / 2 || remaining == total / 4) {
            state.setMaskedWord(revealOneLetter(word, state.getMaskedWord()));
        }
    }

    private synchronized void endRound(Room room) {
        String roomCode = room.getRoomCode();

        if (Boolean.TRUE.equals(roundEnding.get(roomCode))) {
            return;
        }
        roundEnding.put(roomCode, true);

        cancelRoomTimer(roomCode);

        GameState state = room.getGameState();
        state.setPhase(GamePhase.ROUND_END);
        broadcastState(room);

        messagingTemplate.convertAndSend("/topic/rooms/" + roomCode + "/round-end",
                Map.of(
                        "word", state.getCurrentWord(),
                        "scores", room.getPlayers(),
                        "nextDrawerIndex", (state.getDrawerIndex() + 1) % room.getPlayers().size()
                )
        );

        int nextDrawer = state.getDrawerIndex() + 1;
        if (nextDrawer >= room.getPlayers().size()) {
            nextDrawer = 0;
            state.setCurrentRound(state.getCurrentRound() + 1);
        }

        if (state.getCurrentRound() > room.getSettings().getRounds()) {
            state.setPhase(GamePhase.GAME_OVER);
            broadcastState(room);
            Player winner = room.getPlayers().stream()
                    .max(Comparator.comparingInt(Player::getScore))
                    .orElse(null);

            messagingTemplate.convertAndSend("/topic/rooms/" + roomCode + "/game-over",
                    Map.of(
                            "winner", winner,
                            "leaderboard", room.getPlayers()
                    )
            );
            return;
        }

        state.setDrawerIndex(nextDrawer);

        scheduler.schedule(() -> {
            roundEnding.put(roomCode, false);
            startNextTurn(room);
        }, 3, TimeUnit.SECONDS);
    }

    private void startNextTurn(Room room) {
        GameState state = room.getGameState();
        Player drawer = room.getPlayers().get(state.getDrawerIndex());

        state.setPhase(GamePhase.WORD_PICK);
        state.setCurrentDrawerId(drawer.getId());
        state.setCurrentWord(null);
        state.setMaskedWord(null);
        state.setRemainingSeconds(0);
        state.setCurrentWordOptions(wordService.randomWords(room.getSettings().getWordChoices()));

        room.getPlayers().forEach(p -> p.setGuessedCorrectly(false));
        room.getStrokes().clear();
        activeStrokes.clear();

        broadcastState(room);

        messagingTemplate.convertAndSend(
                "/topic/rooms/" + room.getRoomCode() + "/word-options/" + drawer.getId(),
                state.getCurrentWordOptions()
        );
    }

    private void broadcastState(Room room) {
        Room safeRoom = sanitizeRoomForClients(room);
        messagingTemplate.convertAndSend("/topic/rooms/" + room.getRoomCode() + "/state", safeRoom);
    }

    private Room sanitizeRoomForClients(Room room) {
        Room copy = new Room();
        copy.setRoomCode(room.getRoomCode());
        copy.setSettings(room.getSettings());
        copy.setPlayers(new ArrayList<>(room.getPlayers()));
        copy.setStrokes(new ArrayList<>(room.getStrokes()));

        GameState gs = new GameState();
        gs.setPhase(room.getGameState().getPhase());
        gs.setCurrentRound(room.getGameState().getCurrentRound());
        gs.setDrawerIndex(room.getGameState().getDrawerIndex());
        gs.setCurrentDrawerId(room.getGameState().getCurrentDrawerId());
        gs.setCurrentWordOptions(Collections.emptyList());
        gs.setRemainingSeconds(room.getGameState().getRemainingSeconds());

        if (room.getGameState().getPhase() == GamePhase.DRAWING) {
            gs.setMaskedWord(room.getGameState().getMaskedWord());
        }

        copy.setGameState(gs);
        return copy;
    }

    private int calculateGuesserPoints(int remainingSeconds) {
        return 50 + remainingSeconds;
    }

    private String maskWord(String word) {
        StringBuilder sb = new StringBuilder();
        for (char c : word.toCharArray()) {
            if (c == ' ') {
                sb.append("  ");
            } else {
                sb.append("_ ");
            }
        }
        return sb.toString().trim();
    }

    private String revealOneLetter(String word, String maskedWord) {
        if (maskedWord == null || maskedWord.isBlank()) {
            return maskWord(word);
        }

        String[] masked = maskedWord.split(" ");
        List<Integer> hiddenIndexes = new ArrayList<>();

        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) != ' ' && "_".equals(masked[i])) {
                hiddenIndexes.add(i);
            }
        }

        if (hiddenIndexes.isEmpty()) {
            return maskedWord;
        }

        int randomIndex = hiddenIndexes.get(new Random().nextInt(hiddenIndexes.size()));
        masked[randomIndex] = String.valueOf(word.charAt(randomIndex));

        return String.join(" ", masked);
    }

    private void cancelRoomTimer(String roomCode) {
        ScheduledFuture<?> future = roomTimers.remove(roomCode);
        if (future != null) {
            future.cancel(true);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String resolveSelectedWord(List<String> options, String requestedWord) {
        String normalized = normalize(requestedWord);
        return options.stream()
                .filter(option -> normalize(option).equals(normalized))
                .findFirst()
                .orElse(null);
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }

        String normalized = Normalizer.normalize(text, Normalizer.Form.NFKC)
                .replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase(Locale.ROOT);

        return normalized;
    }
}
