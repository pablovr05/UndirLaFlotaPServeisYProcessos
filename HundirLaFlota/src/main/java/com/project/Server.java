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
            System.out.println("Esperando conexiones...");
            while (true) {

                Socket socket = servidorSocket.accept();
                BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);

                String playerName = entrada.readLine();
                Player player = new Player(playerName, socket);
                
                System.out.println(player);

                synchronized (currentServerUsers) {
                    currentServerUsers.add(player);
                    currentInGameUsers.add(player);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
