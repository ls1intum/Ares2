package de.tum.cit.ase.ares.api.ui;

import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class View {
    private final ViewModel viewModel;
    private final GridPane gridPane;

    public View(ViewModel viewModel) {
        this.viewModel = viewModel;
        this.gridPane = new GridPane();
        initializeUI();
    }

    private void initializeUI() {
        Label projectLabel = new Label("Project to Secure:");
        TextField projectTextField = new TextField();
        projectTextField.setEditable(false);
        Button projectButton = new Button("Choose Folder");
        gridPane.add(projectLabel, 0, 0);
        gridPane.add(projectTextField, 1, 0);
        gridPane.add(projectButton, 2, 0);

        Label policyLabel = new Label("Policy:");
        TextField policyTextField = new TextField();
        policyTextField.setEditable(false);
        Button policyButton = new Button("Choose YAML File");
        gridPane.add(policyLabel, 0, 1);
        gridPane.add(policyTextField, 1, 1);
        gridPane.add(policyButton, 2, 1);

        Button createFilesButton = new Button("Create Files");
        gridPane.add(createFilesButton, 1, 2);

        projectButton.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            Stage stage = (Stage) gridPane.getScene().getWindow();
            File selectedDirectory = directoryChooser.showDialog(stage);
            if (selectedDirectory != null) {
                viewModel.setProjectDirectory(selectedDirectory.getAbsolutePath());
            }
        });

        policyButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("YAML Files", "*.yaml"));
            Stage stage = (Stage) gridPane.getScene().getWindow();
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                viewModel.setPolicyFile(selectedFile.getAbsolutePath());
            }
        });

        viewModel.getProjectDirectorySubject()
                .observeOn(JavaFxScheduler.platform())
                .subscribe(projectTextField::setText);

        viewModel.getPolicyFileSubject()
                .observeOn(JavaFxScheduler.platform())
                .subscribe(policyTextField::setText);

        createFilesButton.setOnAction(event -> {
            if (viewModel.canCreateFiles()) {
                // Implement the logic for creating files here
                System.out.println("Creating files...");
            } else {
                showAlert("Error", "Both Project Directory and Policy File must be selected.");
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public GridPane getGridPane() {
        return gridPane;
    }
}