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

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if(!(object instanceof ComboItem)) {
            return false;
        }

        ComboItem comboItem = (ComboItem) object;

        return comboItem.id == this.id;
    }
}
