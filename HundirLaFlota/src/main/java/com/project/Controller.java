package com.project;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;


public class Controller implements Initializable {

    @FXML
    private static TextField nameField;

    @FXML
    private static TextField ipField;

    @FXML
    private static TextField portField;

    @FXML
    private static Button cancelButton;

    @FXML
    private static Button acceptButton;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        throw new UnsupportedOperationException("Unimplemented method 'initialize'");
    }
}
