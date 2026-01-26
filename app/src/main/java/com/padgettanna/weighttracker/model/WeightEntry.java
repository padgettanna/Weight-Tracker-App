package com.padgettanna.weighttracker.model;

import java.time.LocalDate;

/**
 * Represents a single weight entry recorded by the user.
 * This model is used for data processing and algorithmic analysis,
 * separate from UI and database concerns.
 */
public class WeightEntry implements Comparable<WeightEntry>{
    private int id;
    private final LocalDate date;
    private final int weight;

    public WeightEntry(int id, LocalDate date, int weight) {
        this.id = id;
        this.date = date;
        this.weight = weight;
    }

    public int getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public int compareTo(WeightEntry other) {
        return this.date.compareTo(other.date);
    }
}

