package com.project;

import javafx.application.Platform;
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

import java.io.BufferedReader;
import java.io.IOException;


public class ControllerMatchmaking implements Initializable {
    
    @FXML
    private Button cancelButton;

    @FXML
    private Button acceptButton;

    @FXML
    private Text userName;

    @FXML
    private ComboBox<String> objectivesName;

    private BufferedReader entrada;

    private PrintWriter salida;

    private String nombre;

    @FXML
    private void acceptButtonAction(ActionEvent event) {
        String selectedPlayer = objectivesName.getValue();
        if (selectedPlayer != null && !selectedPlayer.trim().isEmpty()) {
            System.out.println("Se pulsó el botón aceptar y se seleccionó: " + selectedPlayer);
            
            salida.println("SELECCION:" + selectedPlayer);
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

    public void setEntrada(BufferedReader entrada) {
        this.entrada = entrada;

        new Thread(this::recibirMensajes).start();
    }

    public void setSalida(PrintWriter salida) {
        this.salida = salida;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
        userName.setText(nombre);
    }

    public void recibirMensajes() {
        String message;
        try {
            while ((message = entrada.readLine()) != null) {
                if (message.startsWith("PLAYER_LIST:")) {
                    String playerList = message.substring(12); // Obtener la lista de jugadores
                    List<String> jugadores = List.of(playerList.split(",")); // Convertir a lista
                    Platform.runLater(() -> updatePlayerList(jugadores));
                } else if (message.startsWith("MATCH_FOUND:")) {
                    String matchedPlayer = message.substring(12); // Obtener el nombre del jugador con el que se ha hecho match
                    Platform.runLater(() -> notifyMatch(matchedPlayer)); // Notificar al usuario
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Método para notificar al usuario que ha hecho match
    public void notifyMatch(String matchedPlayer) {
        System.out.println("¡Bien! Has hecho match con " + matchedPlayer);
        // Aquí puedes agregar lógica adicional para la interfaz gráfica, por ejemplo mostrar una alerta
    }
}