package com.eduhive.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML private ComboBox<String> navigationComboBox;
    @FXML private StackPane annonceView;
    @FXML private StackPane stageView;
    @FXML private StackPane evenementView;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Setup navigation options
        navigationComboBox.getItems().addAll("Annonces", "Stages", "Événements");
        navigationComboBox.setValue("Annonces");

        // Handle navigation changes
        navigationComboBox.setOnAction(e -> {
            String selected = navigationComboBox.getValue();
            annonceView.setVisible("Annonces".equals(selected));
            stageView.setVisible("Stages".equals(selected));
            evenementView.setVisible("Événements".equals(selected));
        });
    }
} 