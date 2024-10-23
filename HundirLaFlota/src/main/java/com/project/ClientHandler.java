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
    private Player player; // Ahora no es estático, es único por cliente

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

            player = new Player(playerName, socket); // Cada cliente tiene su propio Player
            System.out.println("Jugador conectado: " + playerName);

            synchronized (Server.currentServerUsers) {
                Server.currentServerUsers.add(player);
            }

            broadcastPlayerList();

            String message;
            while ((message = entrada.readLine()) != null) {
                if (message.startsWith("SELECCION:")) {
                    String selectedPlayerName = message.substring(10); // Obtener el nombre del jugador seleccionado
                    player.setSeleccionado(selectedPlayerName);

                    System.out.println(playerName + " seleccionó a " + selectedPlayerName);
                    
                    // Verificar si el jugador seleccionado también seleccionó al jugador actual
                    checkMutualSelection(player, selectedPlayerName);
                } else {
                    System.out.println("Mensaje de " + playerName + ": " + message);
                }
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


    private void checkMutualSelection(Player currentPlayer, String selectedPlayerName) {
        synchronized (Server.currentServerUsers) {
            for (Player p : Server.currentServerUsers) {
                if (p.getNom().equals(selectedPlayerName)) {
                    // Verificar si el jugador seleccionado también seleccionó al jugador actual
                    if (currentPlayer.getNom().equals(p.getSeleccionado())) {
                        // Coincidencia mutua
                        System.out.println("¡Coincidencia mutua entre " + currentPlayer.getNom() + " y " + selectedPlayerName + "!");

                        // Notificar a ambos jugadores
                        sendMatchNotification(currentPlayer, p);
                    }
                    break;
                }
            }
        }
    }

    private void sendMatchNotification(Player player1, Player player2) {
        try {
            PrintWriter pw1 = new PrintWriter(player1.getSocket().getOutputStream(), true);
            PrintWriter pw2 = new PrintWriter(player2.getSocket().getOutputStream(), true);

            System.out.println(pw1);
            System.out.println(pw2);
            
            // Mensajes de depuración
            System.out.println("Notificando a " + player1.getNom() + " que ha hecho match con " + player2.getNom());
            System.out.println("Notificando a " + player2.getNom() + " que ha hecho match con " + player1.getNom());
    
            pw1.println("MATCH_FOUND:" + player2.getNom());
            pw2.println("MATCH_FOUND:" + player1.getNom());
        } catch (IOException e) {
            e.printStackTrace();
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
