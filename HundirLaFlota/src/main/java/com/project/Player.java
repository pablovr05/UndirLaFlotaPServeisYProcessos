package com.project;

import java.net.Socket;

public class Player {

    private String nom;
    private Socket socket;

    public Player(String nom, Socket socket) {
        this.nom = nom;
        this.socket = socket;
    }

    public String getNom() {
        return nom;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public String toString() {
        return "Name: " + nom + " " + socket;
    }
}
