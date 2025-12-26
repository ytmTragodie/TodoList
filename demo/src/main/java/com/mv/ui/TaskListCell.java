package com.mv.ui;

import java.util.Observable;

import com.mv.model.TodoItem;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

public class TaskListCell extends ListCell<TodoItem> {
    private final Runnable onChangedCallback;
    private final HBox root = new HBox(15);
    private final CheckBox doneBox = new CheckBox();
    private final Label contentLabel = new Label();
    private final Label priorityIcon =new Label();
    private final Label idLabel = new Label();
    
    // 进度显示和调节
    private final ProgressBar progressBar = new ProgressBar();


    private final Region spacer = new Region();

    public TaskListCell(Runnable onChangedCallback) {
        super();
        this.onChangedCallback = onChangedCallback;
        
        root.setAlignment(Pos.CENTER_LEFT);
        root.setPadding(new Insets(10,12,10,12));
        root.getStyleClass().add("todoitem-container");


        idLabel.getStyleClass().add("todoitem-id");
        idLabel.setMinWidth(20);
        contentLabel.getStyleClass().add("todoitem-content");
        priorityIcon.getStyleClass().add("todoitem-priority-icon");
        
        // 配置进度条和滑动条
        progressBar.setPrefWidth(100);
        progressBar.setMinHeight(10);

        
        // 设置布局
        root.getChildren().addAll(idLabel, doneBox, priorityIcon, contentLabel, spacer, progressBar);
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        
    }


    @Override
    protected void updateItem(TodoItem item, boolean empty) {
        super.updateItem(item, empty);
        

        doneBox.selectedProperty().unbind();
        doneBox.setOnAction(null);
        progressBar.progressProperty().unbind();
    
        root.getStyleClass().remove("completed");
        if(empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            //双向绑定
            //doneBox.selectedProperty().bindBidirectional(item.completedProperty());
            doneBox.setSelected(item.isCompleted());

            // 初始样式
            updateItemStyle(item.isCompleted());
            updatePriorityIcon(item.getPriority());
            idLabel.setText(String.valueOf(item.getId()));
            contentLabel.setText(item.getContent());
            // 绑定进度条
            progressBar.progressProperty().bind(item.progressProperty().divide(100.0));
            
            //点击donebox 后样式更新
            doneBox.setOnAction(e-> {
                boolean selected = doneBox.isSelected();
                // 同步回数据模型
                item.setCompleted(selected);
                updateItemStyle(selected);
                if(onChangedCallback != null) {
                    onChangedCallback.run();
                }
            });
            setGraphic(root);
        }
    }

    private void updatePriorityIcon(int priority) {
        switch (priority) {
            case 2:
                priorityIcon.setText("○");
                priorityIcon.setStyle("-fx-text-fill: red;");
                break;
            case 1:
                priorityIcon.setText("○");
                priorityIcon.setStyle("-fx-text-fill: orange;");
                break;
            default:
                priorityIcon.setText("○");
                priorityIcon.setStyle("-fx-text-fill: green;");
                break;
        }
    }

    private void updateItemStyle(boolean completed) {
        // 修改：操作 root (HBox) 的样式，而不是 Cell 本身
        if (completed) {
            if (!root.getStyleClass().contains("completed")) {
                root.getStyleClass().add("completed");
            }
        } else {
            root.getStyleClass().remove("completed");
        }
    }

}
