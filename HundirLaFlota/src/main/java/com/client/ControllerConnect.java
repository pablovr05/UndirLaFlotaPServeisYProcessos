package com.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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

    public static WebSocketClient clienteWebSocket;

    public static String nombre, nombreEnemigo;

    public static ControllerConnect instance;

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
        instance = this;
        ipField.setText("localhost");
        portField.setText("12345");
    }

    private void establecerConexión() {
        nombre = nameField.getText().trim();
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
                    sendMessage("{\"type\":\"setName\",\"name\":\"" + nombre + "\"}");

                    System.out.println("Se cambia la interfaz a matchmaking");

                    UtilsViews.cambiarFrame("/assets/layout_matchmaking.fxml");

                }

                @Override
                public void onMessage(String message) {
                    JSONObject obj = new JSONObject(message);

                    if (obj.has("type")) {
                        String type = obj.getString("type");
                        if ("clients".equals(type)) {
                            JSONArray clientArray = obj.getJSONArray("list");
                            List<String> clientNames = new ArrayList<>();
                            for (int i = 0; i < clientArray.length(); i++) {
                                clientNames.add(clientArray.getString(i));
                            }

                            // Actualizar la lista de jugadores en el ComboBox
                            Platform.runLater(() -> {
                                ControllerMatchmaking.instance.updatePlayerList(clientNames);
                            });

                        } else if ("matchConfirm".equals(type)) {
                            String enemyName = obj.getString("enemyName");
                            System.out.println("Combate aceptado"); 
                            System.out.println("Inicio de combate contra: " + enemyName); 
                            UtilsViews.cambiarFrame("/assets/layout_viewplay.fxml"); 

                        } else if ("serverSelectableObjects".equals(type)) {
                            ControllerPlay.instance.setSelectableObjects(obj.getJSONObject("selectableObjects"));
                        } else if ("readyToStart".equals(type)) {
                            nombreEnemigo = obj.getString("enemyName");
                            System.out.println("Empezando combate contra: " + nombreEnemigo);
                            
                            JSONObject barcosJugador = ControllerPlay.instance.getAllShipsAsJSON();

                            sendShipsToServer(barcosJugador);

                            UtilsViews.cambiarFrame("/assets/layout_match.fxml");
                        } else if ("mouseMoved".equals(type)) {

                            double mouseX = obj.getDouble("x");
                            double mouseY = obj.getDouble("y");
                            String clientId = obj.getString("clientId");

                            //System.out.println("Se actualiza el cursor en el cliente del enemigo");
                            
                            // Actualizar la posición del cursor en la interfaz del cliente enemigo
                            ControllerMatch.updateCursorPosition(mouseX, mouseY, clientId);
                        } else if ("attackResult".equals(type)){
                            int col = obj.getInt("col");
                            int row = obj.getInt("row");
                            boolean hit = obj.getBoolean("hit");
                            String attackerId = obj.getString("attacker");

                            if (nombre.equals(attackerId)) {
                                ControllerMatch.instance.paintEnemyGrid(col, row, hit);
                                System.out.println("Ataque realizado en: " + col + ", " + row);
                            } else {
                                ControllerMatch.instance.paintPlayerGrid(col, row, hit);
                                System.out.println("Ataque recibido en: " + col + ", " + row);
                            }


                        } else if ("gameOver".equals(type)) {
                            String winner = obj.getString("winner");
                            System.out.println("Juego terminado. Ganador: " + winner);
                            UtilsViews.cambiarFrame("/assets/layout_matchmaking.fxml");
                        }
                    }
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

    public void sendMessage(String message) {
        if (clienteWebSocket != null && clienteWebSocket.isOpen()) {
            clienteWebSocket.send(message);
        } else {
            System.out.println("No se puede enviar el mensaje. Conexión no está abierta.");
        }
    }

    private void sendShipsToServer(JSONObject barcosJugador) {
        // Crear el objeto JSON que incluirá los barcos
        JSONObject mensaje = new JSONObject();
        mensaje.put("type", "playerShips"); // Tipo de mensaje
        mensaje.put("playerName", ControllerConnect.nombre); // Tipo de mensaje
        mensaje.put("ships", barcosJugador); // Incluye los barcos del jugador
    
        ControllerConnect.clienteWebSocket.send(mensaje.toString());
    }
}
