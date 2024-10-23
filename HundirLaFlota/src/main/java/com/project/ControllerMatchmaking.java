package com.project;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
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
    private BufferedReader entrada;  

    private String nombre;

    @FXML
    private void acceptButtonAction(ActionEvent event) {
        String selectedPlayer = objectivesName.getValue();
        if (selectedPlayer != null && !selectedPlayer.trim().isEmpty()) {
            System.out.println("Se pulsó el botón aceptar y se seleccionó: " + selectedPlayer);
            
            salida.println("SELECCION:" + selectedPlayer); // Envía la selección al servidor
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

    // Método para actualizar la lista de jugadores en el ComboBox
    public void updatePlayerList(List<String> jugadores) {
        System.out.println("Actualizando lista de jugadores: " + jugadores);
        objectivesName.getItems().clear();
        for (String jugador : jugadores) {
            if (!jugador.trim().isEmpty() && !jugador.trim().equals(nombre)) {
                objectivesName.getItems().add(jugador.trim());
            }
        }
    }

    // Método para establecer el BufferedReader (entrada) y comenzar a recibir mensajes
    public void setEntrada(BufferedReader entrada) {
        System.out.println("SE METIO ENTRADA");
        this.entrada = entrada;

        // Iniciar el hilo que escucha los mensajes del servidor
        new Thread(this::recibirMensajes).start();
    }

    // Método para establecer el PrintWriter (salida)
    public void setSalida(PrintWriter salida) {
        this.salida = salida;
    }

    // Método para establecer el nombre del usuario
    public void setNombre(String nombre) {
        this.nombre = nombre;
        userName.setText(nombre);
    }

    // Método que escucha los mensajes que llegan del servidor
    public void recibirMensajes() {
        System.out.println("inicio de captar mensajes");
        String message;
        try {
            // Bucle para recibir mensajes del servidor
            while ((message = entrada.readLine()) != null) {
                System.out.println("Se recibió mensaje desde ControllerMatchMaking: " + message);
                if (message.startsWith("PLAYER_LIST:")) {
                    String playerList = message.substring(12); // Obtener la lista de jugadores
                    List<String> jugadores = List.of(playerList.split(",")); // Convertir a lista
                    
                    // Actualizar la lista de jugadores en el ComboBox (en el hilo de la UI)
                    Platform.runLater(() -> updatePlayerList(jugadores));
                } else if (message.startsWith("MATCH_FOUND:")) {
                    String matchedPlayer = message.substring(12); // Obtener el nombre del jugador con el que se hizo match
                    
                    // Notificar al usuario sobre el match (en el hilo de la UI)
                    Platform.runLater(() -> notifyMatch(matchedPlayer));

                    cambiarInterfazPonerBarcos();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para notificar al usuario que ha hecho match
    public void notifyMatch(String matchedPlayer) {
        System.out.println("¡Has hecho match con " + matchedPlayer + "!");

        // Mostrar una alerta o actualizar algún componente de la interfaz gráfica
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("¡Match encontrado!");
        alert.setHeaderText(null);
        alert.setContentText("¡Has hecho match con " + matchedPlayer + "!");
        alert.showAndWait();
    }

    private ControllerMatchmaking matchmakingController;

    private void cambiarInterfazPonerBarcos() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/assets/viewPlay.fxml"));
            Parent root = loader.load();

            //matchmakingController = loader.getController();

            //matchmakingController.setNombre(nombre);

            //matchmakingController.setEntrada(entrada);
           
            //matchmakingController.setSalida(salida);

            Stage stage = (Stage) acceptButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
