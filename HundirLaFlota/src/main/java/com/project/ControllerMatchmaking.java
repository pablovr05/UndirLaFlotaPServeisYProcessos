package com.project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.text.Text;

import java.io.PrintWriter;
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

    private PrintWriter salida;

    private String nombre;

    @FXML
    private void acceptButtonAction(ActionEvent event) {
        System.out.println("Se pulsó el botón aceptar");
    }

    @FXML
    private void cancelButtonAction(ActionEvent event) {
        System.out.println("Se pulsó el botón cancelar");
        System.out.println(Server.currentInGameUsers);
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

    public void setSalida(PrintWriter salida) {
        this.salida = salida;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
        userName.setText(nombre);
    }
}
