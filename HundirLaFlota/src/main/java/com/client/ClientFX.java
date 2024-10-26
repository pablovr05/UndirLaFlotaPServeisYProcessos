package com.client;

import org.java_websocket.WebSocket;

public class ClientFX {

    private String nombre; 
    private WebSocket clienteWebSocket; 
    private String selectedPlayerName = null;

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

    public String getSelectedPlayerName() {
        return selectedPlayerName;
    }

    public void setSelectedPlayerName(String selectedPlayerName) {
        this.selectedPlayerName = selectedPlayerName;
    }
}
