package com.mv;


import java.util.ArrayList;
import java.util.List;

import com.mv.model.TodoItem;
import com.mv.model.TodoList;
import com.mv.service.StorageService;
import com.mv.ui.TaskListCell;
import com.mv.ui.TodoItemDialog;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
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


    @Override
    public void start(Stage stage) {
        
  
        //1. 初始化数据
        loadData();

        //2. 核心布局UI 组件
        HBox root = new HBox();
        root.getStyleClass().add("root-container");


        VBox sidebar = createSidebar();
        VBox mainContent = createMainContent();
        
        HBox.setHgrow(mainContent, Priority.ALWAYS);
        root.getChildren().addAll(sidebar, mainContent);

        // 3. 场景设置
        Scene scene = new Scene(root, 900, 600);
        // 确保你的 resources 目录下有 style.css
        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Style sheet not found");
        }

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

    /* 业务逻辑方法 */

    private void switchList(TodoList newList) {
        this.selectedList = newList;
        currentListNameLabel.setText(selectedList.getTitle());
        currentItems.clear();
        currentItems.setAll(selectedList.getItems());
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
            selectedList.getItems().add(newItem);
            saveAll();
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
                saveAll();
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
