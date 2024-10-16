package com.project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ResourceBundle;
import java.io.*;
import java.net.*;

public class ControllerMatchmaking implements Initializable {
    
    @FXML
    private Button cancelButton;

    @FXML
    private Button acceptButton;

    @FXML
    private Text userName;

    @FXML
    private ComboBox objectivesName;

    @FXML
    private void acceptButtonAction(ActionEvent event) {
        System.out.println("Se pultó el botón aceptar");
    }

    @FXML
    private void cancelButtonAction(ActionEvent event) {
        System.out.println("Se pulsó el botón cancelar");

    } 

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }


    
    

    
}
