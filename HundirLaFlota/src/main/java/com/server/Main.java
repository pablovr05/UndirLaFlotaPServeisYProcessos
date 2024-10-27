package com.server;

import com.client.ClientFX;

import javafx.fxml.JavaFXBuilderFactory;

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

public class Main extends WebSocketServer {

    private List<ClientFX> clients; // Lista de clientes conectados

    public Main(InetSocketAddress address) {
        super(address);
        clients = new ArrayList<>();
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

                    System.out.println(1);

                    for (ClientFX cliente : clients) {
                        System.out.println(cliente);
                    }

                    System.out.println(2);

                    if (clienteSeleccionado != null) {
                        System.out.println("Cliente " + clienteSeleccionando.getNombre() + " ha seleccionado a " + clienteSeleccionado.getNombre());
                        if (checkMutualSelection(clienteSeleccionando, clienteSeleccionado)) {
                            if (sendMatchConfirmedMessages(clienteSeleccionando, clienteSeleccionado)) {
                                System.out.println("El mensaje se envió correctamente a: " + clienteSeleccionando.getNombre());
                            } else {
                                System.out.println("Hubo un error en el envio del mensaje a " + clienteSeleccionando.getNombre());
                            }
                            if (sendMatchConfirmedMessages(clienteSeleccionado, clienteSeleccionando)) {
                                System.out.println("El mensaje se envió correctamente a: " + clienteSeleccionado.getNombre());
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

}
