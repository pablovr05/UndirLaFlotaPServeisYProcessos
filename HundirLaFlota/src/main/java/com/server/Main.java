package com.server;

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

import java.util.Random;

public class Main extends WebSocketServer {

    private List<ClientFX> clients; // Lista de clientes conectados

    private Map<String, JSONObject> clientMousePositions = new HashMap<>();
    private static Map<String, JSONObject> selectableObjects = new HashMap<>();
    private static Map<String, Map<String, JSONObject>> clientSelectableObjects;

    private Map<String, Tablero> usersBoats = new HashMap<>();

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
        String clientId = null;
        for (ClientFX cliente : clients) {
            if (cliente.getClienteWebSocket() == conn) {
                clientId = cliente.getNombre();
                break;
            }
        }

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

                case "clientSelectableObjectMoving":
                    String objectId = obj.getString("objectId");
                    clientSelectableObjects.get(clientId).put(objectId, obj);
                    sendServerSelectableObjects(conn);
                    break;

                case "clientMouseMoving":
                    // Manejar el movimiento del mouse del cliente
                    clientMousePositions.put(clientId, obj);
                    JSONObject rst0 = new JSONObject();
                    rst0.put("type", "serverMouseMoving");
                    rst0.put("positions", clientMousePositions);
                    // Usar broadcastMessage para enviar a todos los clientes
                    broadcastMessage(rst0.toString(), conn);
                    break;

                case "playerAccepted":
                    String player = obj.getString("player");
                    String selectingPlayer = obj.getString("selectingPlayer");
                    String socketId = obj.getString("socketId");
                    String messageToDisplay = String.format("Jugador %s (socketId: %s) ha seleccionado a jugador %s", selectingPlayer, socketId, player);
                    System.out.println(messageToDisplay);

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
                case "playerReady":
                    String playerReady = obj.getString("name");
                    String socketIdReady = obj.getString("socketId");
                    String enemyNameReady = obj.optString("enemyName", null); // Permitir `null` si falta `enemyName`
                
                    System.out.printf("Jugador %s (socketId: %s) está listo para empezar contra %s%n", playerReady, socketIdReady, enemyNameReady);
                
                    ClientFX clienteUser = null;
                    boolean exit = false;
                
                    // Buscar y actualizar `clienteUser`
                    for (ClientFX cliente : clients) {
                        if (cliente.getNombre().equals(playerReady)) {
                            clienteUser = cliente;
                            if (enemyNameReady == null || enemyNameReady.isEmpty()) {
                                clienteUser.setReadyToStartAgainst(null);
                                exit = true;
                            } else {
                                clienteUser.setReadyToStartAgainst(enemyNameReady); // Solo el nombre
                            }
                            break;
                        }
                    }
                
                    if (exit) {
                        System.out.println("ES NULL"); // Solo se imprime si `enemyNameReady` es `null` o vacío
                        break;
                    }
                
                    // Buscar `clienteEnemy` por el nombre
                    ClientFX clienteEnemy = null;
                    for (ClientFX cliente : clients) {
                        if (cliente.getNombre().equals(enemyNameReady)) {
                            clienteEnemy = cliente;
                            break;
                        }
                    }
                
                    if (clienteEnemy == null) {
                        System.out.println("Oponente no encontrado.");
                        break;
                    }
                
                    // Confirmar si ambos jugadores están listos para jugar entre sí
                    if (clienteUser.getReadyToStartAgainst().equals(clienteEnemy.getNombre()) &&
                        clienteEnemy.getReadyToStartAgainst() != null &&
                        clienteEnemy.getReadyToStartAgainst().equals(clienteUser.getNombre())) {
                        System.out.println("Todo listo para empezar");

                        if (sendReadyToStartMessage(clienteUser, clienteEnemy)) {
                            System.out.println("Se envió el mensaje de empezar a " + clienteUser.getNombre());
                        } else {
                            System.out.println("Error al enviar el mensaje");
                        }

                        if (sendReadyToStartMessage(clienteEnemy, clienteUser)) {
                            System.out.println("Se envió el mensaje de empezar a " + clienteEnemy.getNombre());
                        } else {
                            System.out.println("Error al enviar el mensaje"); 
                        }  

                        String uName = clienteUser.getNombre();
                        WebSocket uWebSocket = clienteUser.getClienteWebSocket();
                        String eName = clienteEnemy.getNombre();
                        WebSocket eWebSocket = clienteEnemy.getClienteWebSocket();

                        new Thread(() -> {
                            try {
                                Thread.sleep(50);
                                startBattle(uName, uWebSocket, eName, eWebSocket);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start(); 

                    } else {
                        System.out.println("Tu oponente no está listo para empezar");
                    }
                    break; 
                case "playerShips":
                    // Obtener los barcos enviados por el cliente
                    JSONObject barcosDelJugador = obj.getJSONObject("ships");
                    String nombreJugador = obj.getString("playerName");
                    
                    if (nombreJugador != null) {
                        System.out.println("Barcos de " + clientId + " recibidos: " + barcosDelJugador.toString());
                        Tablero tableroJugador = new Tablero(10); // Initialize player's board
                    
                        // Iterate through the ships
                        tableroJugador.cargarBarcosDesdeJSON(barcosDelJugador);

                        infoTableros(tableroJugador, nombreJugador);

                        usersBoats.put(nombreJugador, tableroJugador);

                    } else {
                        System.out.println("ID de cliente no encontrado para recibir barcos.");
                    }

                    break;  
                case "mouseMoved":
                    broadcastMessage(obj.toString(), conn); // Envía a todos menos al remitente
                    break;
                case "attackShip":
                    // Obtener las coordenadas del ataque
                    int col = obj.getInt("col");
                    int row = obj.getInt("row");

                    // Comprobar el tablero del oponente
                    String enemyId = obj.getString("enemyId");
                    Tablero tableroEnemigo = usersBoats.get(enemyId);
                    boolean hit = tableroEnemigo.descubrirCelda(row, col);

                    JSONObject hitMessage = new JSONObject();
                    hitMessage.put("type", "attackResult");
                    hitMessage.put("attacker", clientId);
                    hitMessage.put("col", col);
                    hitMessage.put("row", row);
                    hitMessage.put("hit", hit);

                    for (ClientFX client : clients) {
                        if (client.getNombre().equals(enemyId) || client.getNombre().equals(clientId)) {
                            WebSocket conn2 = client.getClienteWebSocket();
                            try {
                                conn2.send(hitMessage.toString());
                            } catch (WebsocketNotConnectedException e) {
                                System.out.println("Cliente no conectado: " + client.getNombre());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }                    }
                    break;
            }
        }
    }

    private void broadcastMessage(String message, WebSocket sender) {
        for (ClientFX client : clients) {
            WebSocket conn = client.getClienteWebSocket(); // Obtén la conexión WebSocket del cliente
            if (conn != sender) { // Evita enviar el mensaje al remitente
                try {
                    conn.send(message); // Envía el mensaje
                } catch (WebsocketNotConnectedException e) {
                    System.out.println("Cliente " + client.getNombre() + " no conectado.");
                    // Elimina el cliente de la lista si ya no está conectado
                    clients.remove(client);
                } catch (Exception e) {
                    e.printStackTrace(); // Manejo de excepciones genéricas
                }
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

    private boolean sendReadyToStartMessage(ClientFX clienteUsuario, ClientFX clienteEnemigo) {
        JSONObject message = new JSONObject();
        message.put("type", "readyToStart");
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

    private synchronized void infoTableros(Tablero tableroX, String nombreJugador) {
        System.out.println("Información del Tablero del Jugador :" + nombreJugador);
        tableroX.mostrarTablero(); // Muestra el tablero del jugador X
    }

    public static void addShipsToGrid() {
        // Add objects
        String name0 = "00";
        JSONObject obj0 = new JSONObject();
        obj0.put("objectId", name0);
        obj0.put("x", 450);
        obj0.put("y", 20);
        obj0.put("cols", 1);
        obj0.put("rows", 1);
        selectableObjects.put(name0, obj0);

        String name1 = "01";
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

    public void startBattle(String usuario, WebSocket webSocketUsuario, String enemigo,WebSocket webSocketEnemigo) {
        Random random = new Random();
        int firstTurn = random.nextInt(2);

        JSONObject messageStart = new JSONObject();
        messageStart.put("type", "userTurn");
        messageStart.put("userName", usuario);
        messageStart.put("enemyName", enemigo);

        JSONObject messageSecond = new JSONObject();
        messageSecond.put("type", "enemyTurn");
        messageSecond.put("userName", usuario);
        messageSecond.put("enemyName", enemigo);

        if (firstTurn == 0) {
            webSocketUsuario.send(messageStart.toString());
            webSocketEnemigo.send(messageSecond.toString());

            System.out.println(1);
            System.out.println(messageStart);
            System.out.println(messageSecond);

        } else {
            webSocketEnemigo.send(messageStart.toString());
            webSocketUsuario.send(messageSecond.toString(firstTurn));

            System.out.println(2);
            System.out.println(messageStart);
            System.out.println(messageSecond);
        }   
    }
}