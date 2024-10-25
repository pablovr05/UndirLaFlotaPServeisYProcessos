package com.client;

import org.java_websocket.client.WebSocketClient;

public class ClientFX {

    private static ClientFX instance;
    private String nombre;
    private WebSocketClient clienteWebSocket;

    private ClientFX(String nombre, WebSocketClient clienteWebSocket) {
        this.nombre = nombre;
        this.clienteWebSocket = clienteWebSocket;
    }

    public static synchronized boolean setInstance(String nombre, WebSocketClient clienteWebSocket) {
        if (instance == null) {
            instance = new ClientFX(nombre, clienteWebSocket);
            return true;
        }
        return false;
    }

    public static ClientFX getInstance() {
        return instance;
    }

    public String getNombre() {
        return nombre;
    }

    public WebSocketClient getClienteWebSocket() {
        return clienteWebSocket;
    }
}
