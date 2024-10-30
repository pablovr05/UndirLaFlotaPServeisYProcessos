package com.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;



public class ControllerMatchmaking implements Initializable {
    
    @FXML
    private Button cancelButton;

    @FXML
    private Button acceptButton;

    @FXML
    private Text userName;

    @FXML
    private ComboBox<String> objectivesName;

    public static List<String> nameList;

    public static ControllerMatchmaking instance;

    public static String enemyName;

    @FXML
    private void acceptButtonAction(ActionEvent event) {
        String selectedPlayer = objectivesName.getValue();
        if (selectedPlayer != null && !selectedPlayer.trim().isEmpty()) {

            cancelButton.setDisable(false);
            acceptButton.setDisable(true);

            System.out.println("Se pulsó el botón aceptar y se seleccionó: " + selectedPlayer);

            ControllerConnect controllerConnect = ControllerConnect.instance;

            String message = String.format("{\"type\":\"playerAccepted\",\"player\":\"%s\",\"selectingPlayer\":\"%s\",\"socketId\":\"%s\"}", selectedPlayer, ControllerConnect.nombre, ControllerConnect.clienteWebSocket);

            enemyName = selectedPlayer;

            controllerConnect.sendMessage(message);
            
        } else {
            System.out.println("No se ha seleccionado ningún jugador.");
        }
    }

    @FXML
    private void cancelButtonAction(ActionEvent event) {
        
        cancelButton.setDisable(true);
        acceptButton.setDisable(false);
        
        System.out.println("Se pulsó el botón cancelar");

        ControllerConnect controllerConnect = ControllerConnect.instance;

        String message = String.format("{\"type\":\"playerAccepted\",\"player\":\"%s\",\"selectingPlayer\":\"%s\",\"socketId\":\"%s\"}", null, ControllerConnect.nombre, ControllerConnect.clienteWebSocket);

        controllerConnect.sendMessage(message);
    } 

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        setNombre();
        cancelButton.setDisable(true);
        System.out.println("Inicializando matchmaking...");
        
    }

    public void updatePlayerList(List<String> jugadores) {
        System.out.println("Actualizando lista de jugadores: " + jugadores);
        objectivesName.getItems().clear();
        for (String jugador : jugadores) {
            if (!jugador.trim().isEmpty() && !jugador.trim().equals(ControllerConnect.nombre)) {
                objectivesName.getItems().add(jugador.trim());
            }
        }
    }

    private void setNombre() {
        userName.setText(ControllerConnect.nombre);
    }
}