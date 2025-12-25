package com.mv.model;
// content :内容
// completed :是否完成
// progress :进度
// id :唯一标识符
// priority :优先级
// public record TodoItem(String content, boolean completed, int progress, int id, int priority) {
//     // constructor
//     public TodoItem(String content, boolean completed, int progress, int id, int priority) {
//         this.content = content;
//         this.completed = completed;
//         this.progress = progress;
//         this.id = id;
//         this.priority = priority;
//     }

//     // only content and id  constructor
//     public TodoItem(String content, int id) {
//         this(content, false, 0, id, 0);
//     }


// }


import javafx.beans.property.*;

public class TodoItem {
    private final StringProperty content = new SimpleStringProperty();
    private final BooleanProperty completed = new SimpleBooleanProperty();
    private final IntegerProperty progress = new SimpleIntegerProperty();
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty priority = new SimpleIntegerProperty();

    // Jackson 序列化需要的无参构造函数
    public TodoItem() {}

    public TodoItem(String content, boolean completed, int progress, int id, int priority) {
        this.content.set(content);
        this.completed.set(completed);
        this.progress.set(progress);
        this.id.set(id);
        this.priority.set(priority);
    }

    // --- Property Getter (JavaFX 绑定专用) ---
    public StringProperty contentProperty() { return content; }
    public BooleanProperty completedProperty() { return completed; }
    public IntegerProperty progressProperty() { return progress; }

    // --- 普通 Getter 和 Setter (Jackson 序列化专用) ---
    public String getContent() { return content.get(); }
    public void setContent(String value) { content.set(value); }

    public boolean isCompleted() { return completed.get(); }
    public void setCompleted(boolean value) { completed.set(value); }

    public int getProgress() { return progress.get(); }
    public void setProgress(int value) { progress.set(value); }

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }

    public int getPriority() { return priority.get(); }
    public void setPriority(int value) { priority.set(value); }
}