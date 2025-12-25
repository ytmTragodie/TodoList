package com.mv.model;

import java.util.List;

public class TodoList {
    private String title;
    private List<TodoItem> items;
    
    public TodoList() {};

    public TodoList(String title) {
        this.title = title;
    }
    // get and set for title
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    // get and set for items
    public List<TodoItem> getItems() {
        return items;
    }
    public void setItems(List<TodoItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return title;
    }


}
