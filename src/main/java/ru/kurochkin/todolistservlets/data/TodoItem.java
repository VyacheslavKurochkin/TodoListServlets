package ru.kurochkin.todolistservlets.data;

public class TodoItem {
    private int id;
    private String text;
    private boolean isEditing;

    public TodoItem(int id, String text) {
        this.id = id;
        this.text = text;
    }

    public TodoItem(String text) {
        this.text = text;
    }

    public TodoItem(TodoItem item) {
        this.id = item.id;
        this.text = item.text;
        this.isEditing = item.isEditing;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isEditing() {
        return isEditing;
    }

    public void setEditing(boolean editing) {
        this.isEditing = editing;
    }
}
