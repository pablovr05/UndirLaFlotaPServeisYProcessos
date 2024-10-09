package com.project;

import java.io.*;
import java.net.*;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static final int port = 12345;
    private static final int MAX_HILOS = 50;
    public static ArrayList<Socket> currentServerUsers = new ArrayList<Socket>();

    public static void main(String[] args) {

        ExecutorService pool = Executors.newFixedThreadPool(MAX_HILOS);

        try (ServerSocket servidorSocket = new ServerSocket(port)) {
            System.out.println("Esperando conexiones...");
            while (true) {

                Socket socket = servidorSocket.accept();
                System.out.println("Client connectat! " + socket.getInetAddress());

                synchronized (currentServerUsers) {
                    currentServerUsers.add(socket);
                    pool.execute(new GestorDeClientes(socket));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
        }
    }
}
