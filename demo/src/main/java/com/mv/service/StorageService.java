package com.mv.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mv.model.TodoList;



//它负责将 List<TodoList> 转换为 JSON，以及反向解析。
public class StorageService {
    private static final String FILE_PATH = Paths.get(System.getProperty("user.dir"), "todo_data.json").toString();
    private final ObjectMapper objectMapper;
    
    public StorageService() {
        this.objectMapper = new ObjectMapper();
        // 设置导出带缩进
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    // 保存数据到文件
    public void saveData(Object data) throws IOException {
        try {
            objectMapper.writeValue(new File(FILE_PATH), data);
            System.out.println("Data saved to " + FILE_PATH);
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }
    // 从文件加载数据
    public List<TodoList> loadTodoLists() {
        System.out.println("Loading data from " + FILE_PATH);
        try {
            // 如果没有文件就创建一个新的，如果又就打开
            if (!new File(FILE_PATH).exists()) {
                new File(FILE_PATH).createNewFile();
                System.out.println("Data file not found, created new file.");
                return List.of();
            }

            File file = new File(FILE_PATH);
            if (!file.exists()) {
                System.out.println("Data file not found, returning empty list.");
                return List.of();
            }
            return objectMapper.readValue(file,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, TodoList.class));
        } catch (IOException e) {
            System.out.println("Error loading data: " + e.getMessage());
            return List.of();
        }
    }
}
