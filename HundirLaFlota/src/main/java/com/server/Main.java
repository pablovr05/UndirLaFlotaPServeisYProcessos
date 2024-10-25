package com.server;

import org.java_websocket.server.WebSocketServer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.client.ClientFX;

import org.java_websocket.exceptions.WebsocketNotConnectedException;

import java.util.List;
import java.util.ArrayList;

public class Main extends WebSocketServer {

    private List<ClientFX> clients;

    public Main(InetSocketAddress address) {
        super(address);
        clients = new ArrayList<ClientFX>();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        // No asignar nombre predeterminado aquí.
        System.out.println("WebSocket client connected: " + conn);
        // Enviar lista de clientes al nuevo cliente
        sendClientsList();
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String clientName = null;
        for (ClientFX cliente : clients) {
            if(cliente.getClienteWebSocket().equals(conn)) {
                clientName = cliente.getNombre();
                clients.remove(cliente);
                System.out.println("WebSocket client disconnected: " + clientName + " with conn: " + conn);
                break;
            }
        }
        sendClientsList();
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        JSONObject obj = new JSONObject(message);
        
        // Verificar si el mensaje tiene un tipo
        if (obj.has("type")) {
            String type = obj.getString("type");
            switch (type) {
                case "setName":
                    // Establecer el nombre del cliente
                    String clientName = obj.getString("name");
                    ClientFX new_client = ClientFX.setInstance(clientName, conn);
                    System.out.println("Nombre del cliente establecido: " + clientName);
                    sendClientsList(); // Actualiza la lista de clientes
                    break;
                case "clientMouseMoving":
                    // Manejar el movimiento del mouse del cliente
                    break;
                case "clientSelectableObjectMoving":
                    // Manejar el movimiento de objetos seleccionables del cliente
                    break;
            }
        }
    }
   
    private void broadcastMessage(String message, WebSocket sender) {
        for (Map.Entry<WebSocket, String> entry : clients.entrySet()) {
            WebSocket conn = entry.getKey();
            if (conn != sender) {
                try {
                    conn.send(message);
                } catch (WebsocketNotConnectedException e) {
                    System.out.println("Client " + entry.getValue() + " not connected.");
                    clients.remove(conn);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendClientsList() {
        JSONArray clientList = new JSONArray();
        for (String clientName : clients.values()) {
            clientList.put(clientName);
        }

        Iterator<Map.Entry<WebSocket, String>> iterator = clients.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<WebSocket, String> entry = iterator.next();
            WebSocket conn = entry.getKey();
            String clientName = entry.getValue();

            JSONObject rst = new JSONObject();
            rst.put("type", "clients");
            rst.put("id", clientName);
            rst.put("list", clientList);

            try {
                conn.send(rst.toString());
            } catch (WebsocketNotConnectedException e) {
                System.out.println("Client " + clientName + " not connected.");
                iterator.remove();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket server started on port: " + getPort());
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }

    public static void main(String[] args) {

        // WebSockets server
        Main server = new Main(new InetSocketAddress(12345));
        server.start();
        
        LineReader reader = LineReaderBuilder.builder().build();
        System.out.println("Server running. Type 'exit' to gracefully stop it.");

        try {
            while (true) {
                String line = null;
                try {
                    line = reader.readLine("> ");
                } catch (UserInterruptException e) {
                    continue;
                } catch (EndOfFileException e) {
                    break;
                }

                line = line.trim();

                if (line.equalsIgnoreCase("exit")) {
                    System.out.println("Stopping server...");
                    try {
                        server.stop(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                } else {
                    System.out.println("Unknown command. Type 'exit' to stop server gracefully.");
                }
            }
        } finally {
            System.out.println("Server stopped.");
        }
    }
}
