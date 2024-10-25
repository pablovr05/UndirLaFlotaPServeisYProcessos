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

    private String nombre;

    @FXML
    private void acceptButtonAction(ActionEvent event) {
        String selectedPlayer = objectivesName.getValue();
        if (selectedPlayer != null && !selectedPlayer.trim().isEmpty()) {
            System.out.println("Se pulsó el botón aceptar y se seleccionó: " + selectedPlayer);
            
        } else {
            System.out.println("No se ha seleccionado ningún jugador.");
        }
    }

    @FXML
    private void cancelButtonAction(ActionEvent event) {
        System.out.println("Se pulsó el botón cancelar");
    } 

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Inicializando matchmaking...");
        
    }

    public void updatePlayerList(List<String> jugadores) {
        System.out.println("Actualizando lista de jugadores: " + jugadores);
        objectivesName.getItems().clear();
        for (String jugador : jugadores) {
            if (!jugador.trim().isEmpty() && !jugador.trim().equals(nombre)) {
                objectivesName.getItems().add(jugador.trim());
            }
        }
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
        userName.setText(nombre);
    }
}