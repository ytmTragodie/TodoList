package com.mv.ui;

import com.mv.model.TodoItem;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class TodoItemDialog extends Dialog<TodoItem> {
    public TodoItemDialog(TodoItem item) {
        setTitle(item == null ? "添加代办事项" : "编辑代办事项");
        setHeaderText("请输入代办事项的详细信息");

        ButtonType savButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(savButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20,150,10,10));



        // Create input fields
        TextField contentField =  new TextField();
        contentField.setPromptText("代办内容");
        ChoiceBox<String> priorityChoiceBox = new ChoiceBox<>();
        priorityChoiceBox.getItems().addAll("低", "中", "高");

        Slider progressSlider = new Slider(0, 100, 0);
        progressSlider.setShowTickLabels(true);
        Label progressLabel = new Label("进度: 0%");

        if (item != null) {
            contentField.setText(item.getContent());

            priorityChoiceBox.getSelectionModel().select(item.getPriority());
            progressSlider.setValue(item.getProgress());
            progressLabel.setText("进度: " + item.getProgress() + "%");
        } else {
            priorityChoiceBox.getSelectionModel().select(0);
        }

        // 绑定进度条数值到 Label
        progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> 
            progressLabel.setText(newVal.intValue() + "%"));
        
        // 布局组装
        grid.add(new Label("内容:"), 0, 0);
        grid.add(contentField, 1, 0);
        grid.add(new Label("优先级:"), 0, 1);
        grid.add(priorityChoiceBox, 1, 1);
        grid.add(new Label("进度:"), 0, 2);
        grid.add(progressSlider, 1, 2);
        grid.add(progressLabel, 2, 2);

        getDialogPane().setContent(grid);

        // Result converter
        setResultConverter(dialogButton -> {
            if (dialogButton == savButtonType) {
                // int id = (item != null) ? item.getId() : (int) (System.currentTimeMillis() % 100000);
                int id = 0;// 设置id 默认值为0，实际应用中应由调用方设置唯一ID
                String content = contentField.getText();
                return new TodoItem(
                    content,
                    item != null && item.isCompleted(),
                    (int) progressSlider.getValue(),
                    id,
                    priorityChoiceBox.getSelectionModel().getSelectedIndex()
                );
            }
            return null;
        });
    }

}
