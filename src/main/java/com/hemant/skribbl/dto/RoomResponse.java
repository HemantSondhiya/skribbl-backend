package com.hemant.skribbl.dto;
import com.hemant.skribbl.model.Room;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoomResponse {
    private String playerId;
    private Room room;
}