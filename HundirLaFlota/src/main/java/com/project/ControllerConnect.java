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

import java.io.*;
import java.net.*;
import java.util.ResourceBundle;

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

    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter salida;

    @FXML
    private void acceptButtonAction(ActionEvent event) {
        System.out.println("Se pulsó el botón aceptar");
        establecerConexión();
    }

    @FXML
    private void cancelButtonAction(ActionEvent event) {
        System.out.println("Se pulsó el botón cancelar");
        // Aquí puedes cerrar la aplicación o volver a la pantalla anterior
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicialización si es necesario
    }

    private void establecerConexión() {
        String nombre = nameField.getText().trim();
        String ip = ipField.getText().trim();
        String portText = portField.getText().trim();

        if (nombre.isEmpty() || ip.isEmpty() || portText.isEmpty()) {
            System.out.println("Por favor, completa todos los campos.");
            return;
        }

        try {
            int port = Integer.parseInt(portText);
            socket = new Socket(ip, port);
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(socket.getOutputStream(), true);

            salida.println(nombre);
            System.out.println("Conexión establecida con el servidor.");

            cambiarInterfazMatchmaking(nombre);

            new Thread(() -> {
                try {
                    String message;
                    while ((message = entrada.readLine()) != null) {
                        if (message.startsWith("PLAYER_LIST:")) {
                            String players = message.substring("PLAYER_LIST:".length());
                            String[] playerNames = players.split(",");
                            javafx.application.Platform.runLater(() -> {
                                if (matchmakingController != null) {
                                    matchmakingController.updatePlayerList(java.util.Arrays.asList(playerNames));
                                }
                            });
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (NumberFormatException e) {
            System.out.println("Puerto inválido.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error al conectar con el servidor.");
            e.printStackTrace();
        }
    }

    private ControllerMatchmaking matchmakingController;

    private void cambiarInterfazMatchmaking(String nombre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/assets/layout_matchmaking.fxml"));
            Parent root = loader.load();

            matchmakingController = loader.getController();

            matchmakingController.setNombre(nombre);

            matchmakingController.setEntrada(entrada);
           
            matchmakingController.setSalida(salida);

            Stage stage = (Stage) acceptButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}