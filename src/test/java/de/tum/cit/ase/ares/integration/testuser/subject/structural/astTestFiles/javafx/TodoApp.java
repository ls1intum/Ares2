package de.tum.cit.ase.ares.integration.testuser.subject.structural.astTestFiles.javafx;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TodoApp extends Application {

    private final ObservableList<TodoItem> todoItems = FXCollections.observableArrayList();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("To-Do List Application");

        ListView<TodoItem> listView = new ListView<>(todoItems);
        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(TodoItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getDescription() == null) {
                    setText(null);
                } else {
                    setText(item.isCompleted() ? item.getDescription() + " (Completed)" : item.getDescription());
                }
            }
        });

        TextField inputField = new TextField();
        inputField.setPromptText("Enter new task");

        Button addButton = new Button("Add");
        addButton.setOnAction(e -> {
            String taskDescription = inputField.getText();
            if (!taskDescription.isEmpty()) {
                todoItems.add(new TodoItem(taskDescription));
                inputField.clear();
            }
        });

        Button removeButton = new Button("Remove");
        removeButton.setOnAction(e -> {
            TodoItem selectedItem = listView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                todoItems.remove(selectedItem);
            }
        });

        Button markAsCompletedButton = new Button("Mark as Completed");
        markAsCompletedButton.setOnAction(e -> {
            TodoItem selectedItem = listView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                selectedItem.setCompleted(true);
                listView.refresh();
            }
        });

        HBox inputBox = new HBox(5, inputField, addButton);
        HBox actionBox = new HBox(5, removeButton, markAsCompletedButton);

        VBox layout = new VBox(10, listView, inputBox, actionBox);
        layout.setPadding(new javafx.geometry.Insets(10));

        Scene scene = new Scene(layout, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static class TodoItem {
        private final String description;
        private boolean completed;

        public TodoItem(String description) {
            this.description = description;
            this.completed = false;
        }

        public String getDescription() {
            return description;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }
    }
}

