package com.project;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
        ) {

            String mensaje;
            output.println("Bienvenido al servidor. Escribe 'salir' para desconectar.");

            while ((mensaje = input.readLine()) != null) {
                System.out.println("Mensaje del cliente: " + mensaje);
                if (mensaje.equalsIgnoreCase("salir")) {
                    output.println("Desconectando...");
                    break;
                }
                // Responder al cliente
                output.println("Servidor recibió: " + mensaje);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            synchronized (Server.currentServerUsers) {
                // Eliminar el socket del cliente de la lista
                Server.currentServerUsers.remove(socket);
                System.out.println("Cliente desconectado.");
            }
        } 
    }
} 

    