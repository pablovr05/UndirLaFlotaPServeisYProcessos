package com.project;

import java.io.*;
import java.net.*;

import java.util.ArrayList;

public class Server {

    private static final int port = 12345;
    public static ArrayList<Socket> currentServerUsers = new ArrayList<Socket>();
    public static void main(String[] args) {
        System.out.println("Esperando conexiones...");
        try (ServerSocket servidorSocket = new ServerSocket(port)) {
            while (true) {

                Socket socket = servidorSocket.accept();
                System.out.println("Client connectat!");

                synchronized (currentServerUsers) {
                    currentServerUsers.add(socket);
                }
                
                new Thread(new GestorDeClientes(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
