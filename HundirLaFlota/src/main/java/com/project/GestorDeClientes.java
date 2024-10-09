package com.project;

import java.io.*;
import java.net.*;

public class GestorDeClientes implements Runnable {
    
    private Socket socket;

    public GestorDeClientes(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {

            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            output.println("Conexión establecida");

            System.out.println(3);

            String mensaje;
            while ((mensaje = input.readLine()) != null) {
                System.out.println("Mensaje del cliente: " + mensaje);
                // Aquí puedes hacer lo que necesites con el mensaje
            }

            System.out.println(4);

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

    