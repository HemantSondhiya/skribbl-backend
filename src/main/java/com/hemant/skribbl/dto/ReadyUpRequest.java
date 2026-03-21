package com.hemant.skribbl.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReadyUpRequest {
    @NotBlank
    private String playerId;
    private boolean ready = true;
}
