package com.hemant.skribbl.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinRandomRoomRequest {
    @NotBlank
    private String playerName;
}
