package com.project;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server {

    private static final int port = 12345;
    public static ArrayList<Player> currentServerUsers = new ArrayList<Player>();
    public static ArrayList<Player> currentInGameUsers = new ArrayList<Player>();

    public static void main(String[] args) {

        try (ServerSocket servidorSocket = new ServerSocket(port)) {
            System.out.println("Servidor iniciado. Esperando conexiones en el puerto " + port + "...");
            while (true) {

                Socket socket = servidorSocket.accept();
                System.out.println("Cliente conectado: " + socket.getInetAddress());

                ClientHandler handler = new ClientHandler(socket);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}