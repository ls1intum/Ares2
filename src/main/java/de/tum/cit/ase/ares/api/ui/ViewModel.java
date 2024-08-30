package de.tum.cit.ase.ares.api.ui;

import io.reactivex.rxjava3.subjects.BehaviorSubject;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ViewModel extends Application {
    private final Model model;
    private final BehaviorSubject<String> projectDirectorySubject = BehaviorSubject.create();
    private final BehaviorSubject<String> policyFileSubject = BehaviorSubject.create();

    public ViewModel() {
        this.model = new Model();
    }

    public void setProjectDirectory(String directory) {
        model.setProjectDirectory(directory);
        projectDirectorySubject.onNext(directory);
    }

    public void setPolicyFile(String file) {
        model.setPolicyFile(file);
        policyFileSubject.onNext(file);
    }

    public BehaviorSubject<String> getProjectDirectorySubject() {
        return projectDirectorySubject;
    }

    public BehaviorSubject<String> getPolicyFileSubject() {
        return policyFileSubject;
    }

    public boolean canCreateFiles() {
        return model.getProjectDirectory() != null && !model.getProjectDirectory().isEmpty() &&
                model.getPolicyFile() != null && !model.getPolicyFile().isEmpty();
    }

    @Override
    public void start(Stage primaryStage) {
        View view = new View(this);  // Pass ViewModel to the View
        Scene scene = new Scene(view.getGridPane(), 400, 200);
        primaryStage.setScene(scene);
        primaryStage.setTitle("MVVM JavaFX Application");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}