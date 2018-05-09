package com.project.database;

class ComboItem {
    private final int id;
    private String description;

    public ComboItem(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String toString() {
        return description;
    }
}
