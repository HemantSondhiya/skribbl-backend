package com.hemant.skribbl.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Player {
    private String id;
    private String name;
    private int score;
    private boolean host;
    private boolean connected;
    private boolean guessedCorrectly;
    private boolean ready;
}
