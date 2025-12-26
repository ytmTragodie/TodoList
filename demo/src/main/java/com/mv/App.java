package com.mv;


import java.util.ArrayList;
import java.util.List;

import com.mv.model.TodoItem;
import com.mv.model.TodoList;
import com.mv.service.StorageService;
import com.mv.ui.TaskListCell;
import com.mv.ui.TodoItemDialog;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
/**
 * Hello world!
 *
 */
public class App extends Application
{
    private boolean sidebarVisible = true;
    private final StorageService storageService = new StorageService();

    //数据层
    private ObservableList<TodoList> allLists; //侧边栏所有清单
    private TodoList selectedList; // 当前选中的清单
    private ObservableList<TodoItem> currentItems = FXCollections.observableArrayList(); //当前选中清单的任务项

    // UI
    private ListView<TodoList> sidebarListView;
    private ListView<TodoItem> taskListView;
    private Label currentListNameLabel;
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isCollapsed = false;
    private final double COLLAPSED_WIDTH = 30.0; // 收缩后露出的宽度
    private double ORIGINAL_STAGE_X = 0;


    @Override
    public void start(Stage stage) {
        
  
        //1. 初始化数据
        loadData();

        //2. 核心布局UI 组件
        HBox root = new HBox();
        root.getStyleClass().add("root-container");


        VBox sidebar = createSidebar();
        VBox mainContent = createMainContent();
        Button collapseButton = createCollapseButton(root, stage);
        
        HBox.setHgrow(mainContent, Priority.ALWAYS);
        root.getChildren().addAll(sidebar, mainContent, collapseButton);

        // 允许用户拖动无边框窗口
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        // 3. 场景设置
        Scene scene = new Scene(root, 900, 600);
        scene.setFill(Color.TRANSPARENT); // 设置场景背景透明
        stage.initStyle(StageStyle.TRANSPARENT); // 设置窗口透明    
        // 确保你的 resources 目录下有 style.css
        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Style sheet not found");
        }
        // 4. 舞台设置
        stage.initStyle(StageStyle.UNDECORATED);// 无边框窗口
        stage.setTitle("Modern To-Do List");
        stage.setScene(scene);
        stage.show();

    }
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        launch();
    }


    /* load data */ 
    private void loadData() {
        allLists = FXCollections.observableArrayList(storageService.loadTodoLists());
        if (allLists.isEmpty()) {
            // 默认初始数据
            TodoList defaultList = new TodoList("我的任务");
            defaultList.setItems(new ArrayList<>());
            allLists.add(defaultList);
        }
    }


    /* 模块化UI构建 */

    // 创建侧边栏
    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setId("sidebar"); // 对应 CSS
        sidebar.setPadding(new Insets(15));
        sidebar.setPrefWidth(200);

        Label title = new Label("清单目录");
        title.getStyleClass().add("sidebar-title");

        sidebarListView = new ListView<>(allLists);
        VBox.setVgrow(sidebarListView, Priority.ALWAYS);

        // 【关键逻辑：切换清单】
        sidebarListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                switchList(newVal);
            }
        });

        Button addListBtn = new Button("+ 新建清单");
        addListBtn.setMaxWidth(Double.MAX_VALUE);
        addListBtn.setOnAction(e -> handleAddList());

        sidebar.getChildren().addAll(title, sidebarListView, addListBtn);
        return sidebar;
    }

    // 创建主内容区
    private VBox createMainContent() {
        VBox mainContent = new VBox(15);
        mainContent.setPadding(new Insets(20));
        mainContent.setId("main-content-area"); // 对应 CSS

        currentListNameLabel = new Label("请选择一个清单");
        currentListNameLabel.getStyleClass().add("main-title");

        //绑定自定义的tasklistcell
        taskListView = new ListView<>(currentItems);
        taskListView.setCellFactory(listView -> new TaskListCell(() -> {
            // 当任务项状态改变时保存数据
            sortAndSave();
        }));
        taskListView.getStyleClass().add("task-list-view");
        VBox.setVgrow(taskListView, Priority.ALWAYS);

        // 这里可以继续添加你之前的 Input Area (输入框和添加任务按钮)
        // ... 

        // 2.3 input area
        HBox inputArea = new HBox(10);
        Button addButton = new Button("添加");
        Button editButton = new Button("编辑");
        Button removeButton = new Button("移除");
        Button clearButton = new Button("清空任务列表");
       

        //add item logic
        addButton.setOnAction(e -> handleAddItem());
        // edit item logic
        editButton.setOnAction(e -> handleEditItem());
        // remove item logic
        removeButton.setOnAction(e -> {
            TodoItem selectedItem = taskListView.getSelectionModel().getSelectedItem();
            currentItems.remove(selectedItem);
            selectedList.getItems().remove(selectedItem);
            saveAll();
        });
        // clear all items logic
        clearButton.setOnAction(e -> {
            currentItems.clear();
            selectedList.getItems().clear();
            saveAll();
        });


        inputArea.getChildren().addAll(addButton, editButton, removeButton, clearButton);
        mainContent.getChildren().addAll(currentListNameLabel, taskListView, inputArea);
        return mainContent;
    }

    //创建侧边悬浮按钮  
    private Button createCollapseButton(HBox root, Stage stage) {
        Button collapseButton = new Button("◀");
        collapseButton.setPrefWidth(30);
        collapseButton.setPrefHeight(Double.MAX_VALUE);
        collapseButton.setAlignment(Pos.CENTER);
        HBox.setMargin(collapseButton, new Insets(50,0,50,0));
        collapseButton.getStyleClass().add("collapse-button");
        collapseButton.setOnAction(e -> {
            // stage.setX(0);
            if(!isCollapsed ) {
                ORIGINAL_STAGE_X = stage.getX();
                double moveDistance = stage.getWidth() - COLLAPSED_WIDTH;
                toggleStageAnimation(stage, - moveDistance);
                collapseButton.setText("▶");
                isCollapsed = true;
            } else {
                toggleStageAnimation(stage, ORIGINAL_STAGE_X);
                collapseButton.setText("◀");
                isCollapsed = false;
            }  
        });

        root.setOnMouseClicked(e -> {
            if(isCollapsed ){
                toggleStageAnimation(stage, ORIGINAL_STAGE_X);
                collapseButton.setText("◀");
                isCollapsed = false;
            }
        });
        return collapseButton;

    }

    private void toggleStageAnimation(Stage stage, double targetX) {
        // 创建一个时间轴动画，持续 300 毫秒
        Timeline timeline = new Timeline();
        // 关键帧：在 300ms 内，将 stage 的 x 属性平滑变动到 targetX,注意keyvalue constructor 第一个参数要求是writable value
        DoubleProperty dummyX = new SimpleDoubleProperty(stage.getX());
        dummyX.addListener((obs,oldval,newval)->{
            stage.setX(newval.doubleValue());
        });

        KeyValue keyValue = new KeyValue(dummyX, targetX, Interpolator.EASE_BOTH);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(300), keyValue);
        
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    /* 业务逻辑方法 */

    private void switchList(TodoList newList) {
        this.selectedList = newList;
        currentListNameLabel.setText(selectedList.getTitle());
        currentItems.clear();
        currentItems.setAll(selectedList.getItems());
        saveAll();
    }

    private void handleAddList() {
        // 简单示例：添加一个默认名称的新清单

        TextInputDialog dialog = new TextInputDialog("新清单");
        dialog.setTitle("新建清单");
        dialog.setHeaderText("创建一个新的待办清单");
        dialog.setContentText("请输入清单名称：");
        dialog.showAndWait().ifPresent(name -> {
            TodoList newList = new TodoList(name);
            newList.setItems(new ArrayList<>());
            allLists.add(newList);
            sidebarListView.getSelectionModel().select(newList);
            //保存一次
            saveAll();
        });

    }

    private void handleAddItem() {
        if (selectedList == null) return;
        TodoItemDialog dialog = new TodoItemDialog(null);
        dialog.showAndWait().ifPresent(newItem -> {
            currentItems.add(newItem);
            sortAndSave();
            selectedList.getItems().add(newItem);
            sortAndSave();
        });
    }

    private void handleEditItem() {
        if (selectedList == null) return;
        TodoItem selectedItem = taskListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;

        TodoItemDialog dialog = new TodoItemDialog(selectedItem);
        dialog.showAndWait().ifPresent(editedItem -> {
            // 更新当前列表中的项目
            int index = currentItems.indexOf(selectedItem);
            if (index >= 0) {
                currentItems.set(index, editedItem);
                selectedList.getItems().set(index, editedItem);
                sortAndSave();
            }
        });
    }

    private void sortAndSave() {
        if (currentItems !=null) {
            currentItems.sort((a, b) -> {
                if (a.isCompleted() == b.isCompleted()) {
                    return Integer.compare(b.getPriority(), a.getPriority()); // 优先级高的在前
                }
                return Boolean.compare(a.isCompleted(), b.isCompleted()); // 未完成在前
            });
            //同步修改id
            for (int i = 0; i < currentItems.size(); i++) {
                currentItems.get(i).setId(i + 1);
            }
            // 同步回原始数据模型
            selectedList.setItems(new ArrayList<>(currentItems));

            // 4. 强制 ListView 刷新所有 Cell 的显示
            // 这能解决 ID 显示不及时的问题
            taskListView.refresh();
        }
        saveAll();
    }
    
    private void saveAll() {
        try {
            storageService.saveData(new ArrayList<>(allLists));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
