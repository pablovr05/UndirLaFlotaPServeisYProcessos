package com.project;

import java.net.Socket;

public class Player {
    private String nombre;
    private Socket socket;
    private String seleccionado;

    public Player(String nombre, Socket socket) {
        this.nombre = nombre;
        this.socket = socket;
        this.seleccionado = null;
    }

    public String getNom() {
        return nombre;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(String seleccionado) {
        this.seleccionado = seleccionado;
    }
}
