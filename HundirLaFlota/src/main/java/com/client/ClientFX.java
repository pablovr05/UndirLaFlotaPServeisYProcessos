package com.client;

import org.java_websocket.WebSocket;

public class ClientFX {

    private String nombre; 
    private WebSocket clienteWebSocket; 

    public ClientFX(String nombre, WebSocket clienteWebSocket) {
        this.nombre = nombre;
        this.clienteWebSocket = clienteWebSocket;
    }

    public String getNombre() {
        return nombre;
    }

    public WebSocket getClienteWebSocket() {
        return clienteWebSocket;
    }
}
