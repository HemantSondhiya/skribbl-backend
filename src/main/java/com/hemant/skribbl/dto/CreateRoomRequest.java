package com.hemant.skribbl.dto;

import com.hemant.skribbl.model.RoomSettings;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
;

@Data
public class CreateRoomRequest {
    @NotBlank
    private String hostName;
    private RoomSettings settings = new RoomSettings();
}