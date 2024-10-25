package com.client;

import javafx.application.Platform;  // Import Platform for UI thread handling
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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

    private WebSocketClient clienteWebSocket;

    @FXML
    private void acceptButtonAction(ActionEvent event) {
        System.out.println("Se pulsó el botón aceptar");
        establecerConexión();
    }

    @FXML
    private void cancelButtonAction(ActionEvent event) {
        System.out.println("Se pulsó el botón cancelar");
        // Cierra la ventana o regresa a la pantalla anterior
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ipField.setText("localhost");
        portField.setText("12345");
    }

    private void establecerConexión() {
        String nombre = nameField.getText().trim();
        String ip = ipField.getText().trim();
        String portText = portField.getText().trim();

        if (nombre.isEmpty() || ip.isEmpty() || portText.isEmpty()) {
            System.out.println("Por favor, completa todos los campos.");
            return;
        }

        // Convertir el puerto a un número entero
        int port;
        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException e) {
            System.out.println("El puerto debe ser un número válido.");
            return;
        }

        // Crear la URI del WebSocket
        String uri = "ws://" + ip + ":" + port;
        
        // Crear el cliente WebSocket
        try {
            clienteWebSocket = new WebSocketClient(new URI(uri)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("Conexión establecida con el servidor: " + uri);
                    // Enviar el nombre al servidor
                    clienteWebSocket.send("{\"type\":\"setName\",\"name\":\"" + nombre + "\"}");

                    // Change to the matchmaking view on the JavaFX application thread
                    Platform.runLater(() -> {
                        try {
                            UtilsViews.addView(getClass(), "layout_matchmaking", "/assets/layout_matchmaking.fxml");
                            UtilsViews.setViewAnimating("layout_matchmaking");
                        } catch (Exception e) {
                            e.printStackTrace();  // Corrected method name
                        }
                    });
                }

                @Override
                public void onMessage(String message) {
                    System.out.println("Mensaje recibido: " + message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Conexión cerrada: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    System.out.println("Error en la conexión: " + ex.getMessage());
                }
            };

            // Intentar conectar al servidor
            clienteWebSocket.connect();
        } catch (URISyntaxException e) {
            System.out.println("URI no válida: " + e.getMessage());
        }
    }
}
