package com.project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.ResourceBundle;
import java.io.*;
import java.net.*;


public class ControllerConnect implements Initializable {

    @FXML
    private TextField nameField;

    @FXML
    private TextField ipField;

    @FXML
    private TextField portField;

    @FXML
    private Button cancelButton;

    @FXML
    private Button acceptButton;

    @FXML
    private void acceptButtonAction(ActionEvent event) {
        System.out.println("Se pultó el botón aceptar");
        establecerConexión();
    }

    @FXML
    private void cancelButtonAction(ActionEvent event) {
        System.out.println("Se pulsó el botón cancelar");

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    private void establecerConexión() {
        if (!nameField.getText().isEmpty() && !ipField.getText().isEmpty() && !portField.getText().isEmpty()) {
            try (
            Socket socket = new Socket(ipField.getText(), Integer.valueOf(portField.getText()));
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
            ){

                System.out.println("Conexión establecida");

                salida.println(nameField.getText());

                cambiarInterfazMatchmaking();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    } 

    private void cambiarInterfazMatchmaking() {
        try {
            // Cargar el nuevo archivo FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/assets/layout_matchmaking.fxml"));
            Parent matchmakingView = loader.load();
            
            // Obtener la escena actual desde el botón o algún nodo del evento
            Stage stage = (Stage) nameField.getScene().getWindow();

            // Cambiar la escena a la nueva interfaz
            Scene scene = new Scene(matchmakingView);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            }
    }
}
