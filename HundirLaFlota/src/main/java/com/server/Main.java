package com.server;

import com.client.ClientFX;

import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Main extends WebSocketServer {

    private List<ClientFX> clients; // Lista de clientes conectados

    private static Map<String, JSONObject> selectableObjects = new HashMap<>();
    private static Map<String, Map<String, JSONObject>> clientSelectableObjects;

    public Main(InetSocketAddress address) {
        super(address);
        clients = new ArrayList<>();
        clientSelectableObjects = new ConcurrentHashMap<>();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("WebSocket client connected: " + conn);
        // Enviar lista actualizada de clientes a todos los clientes conectados
        sendClientsList();
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        // Buscar y eliminar al cliente que se desconectó
        Iterator<ClientFX> iterator = clients.iterator();
        while (iterator.hasNext()) {
            ClientFX client = iterator.next();
            if (client.getClienteWebSocket().equals(conn)) {
                System.out.println("WebSocket client disconnected: " + client.getNombre());
                iterator.remove();
                break;
            }
        }
        // Enviar lista actualizada de clientes a todos los clientes conectados
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
                    // Configurar el nombre del cliente y añadirlo a la lista de clientes
                    String clientName = obj.getString("name");
                    ClientFX newClient = new ClientFX(clientName, conn);
                    clients.add(newClient);
                    System.out.println("Nombre del cliente establecido: " + clientName);

                    clientSelectableObjects.put(clientName, new HashMap<>());

                    for (String ship : selectableObjects.keySet()) {
                        clientSelectableObjects.get(clientName).put(ship, selectableObjects.get(ship));
                    }

                    // Enviar lista actualizada de clientes a todos los clientes conectados
                    sendClientsList();
                    break;
                case "clientMouseMoving":
                    // Manejar el movimiento del mouse del cliente
                    break;
                case "clientSelectableObjectMoving":
                    // Manejar el movimiento de objetos seleccionables del cliente
                    break;
                case "playerAccepted":
                    String player = obj.getString("player");
                    String selectingPlayer = obj.getString("selectingPlayer");
                    String socketId = obj.getString("socketId");
                    String messageToDisplay = String.format("Jugador %s (socketId: %s) ha seleccionado a jugador %s", selectingPlayer, socketId, player);
                    System.out.println(messageToDisplay);

                    System.out.println(-1);

                    ClientFX clienteSeleccionado = null;

                    for (ClientFX cliente : clients) {
                        if (cliente.getNombre().equals(player)) { //Cliente seleccionado
                            clienteSeleccionado = cliente;                       }
                    }

                    ClientFX clienteSeleccionando = null;

                    for (ClientFX cliente : clients) {
                        if (cliente.getClienteWebSocket() == conn) { //Cliente que selecciona
                            clienteSeleccionando = cliente;
                            if (clienteSeleccionado != null) {
                                clienteSeleccionando.setSelectedPlayerName(clienteSeleccionado.getNombre());
                            } else {
                                clienteSeleccionando.setSelectedPlayerName(null);
                            }
                        }
                    }

                    for (ClientFX cliente : clients) {
                        System.out.println(cliente);
                    }

                    if (clienteSeleccionado != null) {
                        System.out.println("Cliente " + clienteSeleccionando.getNombre() + " ha seleccionado a " + clienteSeleccionado.getNombre());
                        if (checkMutualSelection(clienteSeleccionando, clienteSeleccionado)) {
                            if (sendMatchConfirmedMessages(clienteSeleccionando, clienteSeleccionado)) {
                                System.out.println("El mensaje se envió correctamente a: " + clienteSeleccionando.getNombre());

                                WebSocket socketIdClienteSeleccionado = clienteSeleccionado.getClienteWebSocket();

                                new Thread(() -> {
                                    try {
                                        Thread.sleep(50);
                                        sendServerSelectableObjects(socketIdClienteSeleccionado);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }).start();

                            } else {
                                System.out.println("Hubo un error en el envio del mensaje a " + clienteSeleccionando.getNombre());
                            }
                            if (sendMatchConfirmedMessages(clienteSeleccionado, clienteSeleccionando)) {
                                System.out.println("El mensaje se envió correctamente a: " + clienteSeleccionado.getNombre());

                                WebSocket socketIdClienteSeleccionando = clienteSeleccionando.getClienteWebSocket();

                                new Thread(() -> {
                                    try {
                                        Thread.sleep(50);
                                        sendServerSelectableObjects(socketIdClienteSeleccionando);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }).start(); // Iniciar el nuevo hilo

                            } else {
                                System.out.println("Hubo un error en el envio del mensaje a " + clienteSeleccionado.getNombre());
                            }
                        }
                    } else {
                        System.out.println("La instancia de clienteseleccionado es null ya que el cliente ha dado a cancelar.");
                        System.out.println("Para el cliente: " + clienteSeleccionando.getNombre() + " ha seleccionado " + null);
                    }

                    break;         
            }
        }
    }

    private void sendClientsList() {
        // Crear un JSON con la lista de nombres de clientes
        JSONArray clientList = new JSONArray();
        for (ClientFX client : clients) {
            clientList.put(client.getNombre());
        }

        // Enviar la lista de clientes a todos los clientes conectados
        JSONObject response = new JSONObject();
        response.put("type", "clients");
        response.put("list", clientList);

        for (ClientFX client : clients) {
            WebSocket conn = client.getClienteWebSocket();
            try {
                conn.send(response.toString());
            } catch (WebsocketNotConnectedException e) {
                System.out.println("Cliente no conectado: " + client.getNombre());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendServerSelectableObjects(WebSocket conn) {
        // Create a JSON object to hold the response
        JSONObject rst1 = new JSONObject();
        
        // Retrieve the client name associated with the WebSocket connection
        ClientFX client = null;
        for (ClientFX cliente : clients) {
            if (cliente.getClienteWebSocket().equals(conn)) {
                client = cliente;
                break;
            }
        }
    
        // If client is found, put the selectable objects into the response
        if (client != null) {
            String clientName = client.getNombre();
            rst1.put("type", "serverSelectableObjects");
            rst1.put("selectableObjects", clientSelectableObjects.get(clientName));
    
            try {
                // Send the JSON response back to the client
                conn.send(rst1.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Client not found for connection: " + conn);
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

    private boolean checkMutualSelection(ClientFX seleccionando, ClientFX seleccionado ) {
        if (seleccionando.getSelectedPlayerName() != null && seleccionado.getSelectedPlayerName() != null) {
            if (seleccionando.getNombre().equals(seleccionado.getSelectedPlayerName()) && seleccionado.getNombre().equals(seleccionando.getSelectedPlayerName())) {
                System.out.println("Ha habido match entre: " + seleccionando.getNombre() + " y " + seleccionado.getNombre());
                return true;
            } else {
                System.out.println(seleccionado.getNombre() + " no ha seleccionado a nadie o no te tiene seleccionado");
            }
        }
        return false;
    }

    private boolean sendMatchConfirmedMessages(ClientFX clienteUsuario, ClientFX clienteEnemigo) {
        JSONObject message = new JSONObject();
        message.put("type", "matchConfirm");
        message.put("enemyName", clienteEnemigo.getNombre());

        WebSocket conn = clienteUsuario.getClienteWebSocket();
        try {
            conn.send(message.toString());
            return true;
        } catch (WebsocketNotConnectedException e) {
            System.out.println("Cliente no conectado: " + clienteUsuario.getNombre());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) {
        Main server = new Main(new InetSocketAddress(12345));
        server.start();

        LineReader reader = LineReaderBuilder.builder().build();
        System.out.println("Server running. Type 'exit' to gracefully stop it.");

        addShipsToGrid();

        try {
            while (true) {
                String line = reader.readLine("> ");
                if ("exit".equalsIgnoreCase(line.trim())) {
                    System.out.println("Stopping server...");
                    server.stop(1000);
                    break;
                } else {
                    System.out.println("Unknown command. Type 'exit' to stop server gracefully.");
                }
            }
        } catch (UserInterruptException | EndOfFileException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Server stopped.");
        }
    }

    public static void addShipsToGrid() {
        // Add objects
        String name0 = "O0";
        JSONObject obj0 = new JSONObject();
        obj0.put("objectId", name0);
        obj0.put("x", 450);
        obj0.put("y", 20);
        obj0.put("cols", 1);
        obj0.put("rows", 1);
        selectableObjects.put(name0, obj0);

        String name1 = "O1";
        JSONObject obj1 = new JSONObject();
        obj1.put("objectId", name1);
        obj1.put("x", 410);
        obj1.put("y", 20);
        obj1.put("cols", 1);
        obj1.put("rows", 3);
        selectableObjects.put(name1, obj1);

        String name2 = "02";
        JSONObject obj2 = new JSONObject();
        obj2.put("objectId", name2);
        obj2.put("x", 450);
        obj2.put("y", 125);
        obj2.put("cols", 1);
        obj2.put("rows", 4);
        selectableObjects.put(name2, obj2);

        String name3 = "03";
        JSONObject obj3 = new JSONObject();
        obj3.put("objectId", name3);
        obj3.put("x", 410); // X - Y posicion dibujo inicial
        obj3.put("y", 125);
        obj3.put("cols", 1);  // Girar cols y rows para girar el barco
        obj3.put("rows", 6);
        selectableObjects.put(name3, obj3);
    }
}
