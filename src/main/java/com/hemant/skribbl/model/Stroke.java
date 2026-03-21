package com.hemant.skribbl.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Stroke {
    private String strokeId;
    private String playerId;
    private String color;
    private int size;
    private List<Point> points = new ArrayList<>();

    @Data
    public static class Point {
        private double x;
        private double y;
    }
}
