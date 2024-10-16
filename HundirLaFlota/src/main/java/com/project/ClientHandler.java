package com.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter salida;
    public static Player player;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String playerName = entrada.readLine();
            if (playerName == null || playerName.trim().isEmpty()) {
                System.out.println("Nombre de jugador inválido. Cerrando conexión.");
                socket.close();
                return;
            }

            player = new Player(playerName, socket);
            System.out.println("Jugador conectado: " + playerName);

            synchronized (Server.currentServerUsers) {
                Server.currentServerUsers.add(player);
            }

            broadcastPlayerList();

            String message;
            while ((message = entrada.readLine()) != null) {
                System.out.println("Mensaje de " + playerName + ": " + message);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (player != null) {
                    synchronized (Server.currentServerUsers) {
                        Server.currentServerUsers.remove(player);
                    }
                    broadcastPlayerList();
                    System.out.println("Jugador desconectado: " + player.getNom());
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void broadcastPlayerList() {
        synchronized (Server.currentServerUsers) {
            StringBuilder sb = new StringBuilder();
            sb.append("PLAYER_LIST:");
            for (Player p : Server.currentServerUsers) {
                sb.append(p.getNom()).append(",");
            }
            String playerListMessage = sb.toString();

            for (Player p : Server.currentServerUsers) {
                try {
                    PrintWriter pw = new PrintWriter(p.getSocket().getOutputStream(), true);
                    pw.println(playerListMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
