package com.client;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class WebSocketExample {
    private static WebSocketClient webSocketClient;

    public static void main(String[] args) {
        try {
            // Cambia la dirección IP o nombre de dominio según corresponda
            URI uri = new URI("ws://ieticloudpro.ieti.cat:12345"); // o la IP directa
            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("Conectado al servidor WebSocket");

                    // Envía un mensaje al servidor con el nombre de usuario
                    String message = "Hola, servidor! Soy el usuario: pvicenteroura";
                    send(message);
                }

                @Override
                public void onMessage(String message) {
                    System.out.println("Mensaje del servidor: " + message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Conexión cerrada. Código: " + code + " Razón: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("Error en la conexión: " + ex.getMessage());
                }
            };

            // Conectar al servidor
            webSocketClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
