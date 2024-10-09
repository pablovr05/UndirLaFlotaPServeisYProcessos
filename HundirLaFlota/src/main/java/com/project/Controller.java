package com.project;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;


public class Controller implements Initializable {

    @FXML
    private TextField nameField;

    @FXML
    private TextField ipField;

    @FXML
    private TextField portField;

    @FXML
    private Button cancelButton, acceptButton, addPlayer, nextLy;

    @FXML
    private VBox playerList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addPlayer.setOnAction(event -> {
            Text player = new Text("Player");
            playerList.getChildren().add(player);
        });
        nextLy.setOnAction(event -> {
            UtilsViews.setView("ViewPlaceGrid");
        });
    }
}
