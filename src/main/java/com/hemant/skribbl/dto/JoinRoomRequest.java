package com.hemant.skribbl.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinRoomRequest {
    @NotBlank
    private String playerName;
}
