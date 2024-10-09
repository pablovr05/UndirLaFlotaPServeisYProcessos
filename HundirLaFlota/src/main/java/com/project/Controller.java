package com.project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import java.util.ResourceBundle;
import java.io.*;
import java.net.*;


public class Controller implements Initializable {

    @FXML
    private TextField nameField;

    @FXML
    private TextField ipField;

    @FXML
    private TextField portField;

    @FXML
    private Button cancelButton;

    @FXML
    private Button acceptButton;

    @FXML
    private void acceptButtonAction(ActionEvent event) {
        System.out.println("Se pultó el botón aceptar");
        establecerConexión();
    }

    @FXML
    private void cancelButtonAction(ActionEvent event) {
        System.out.println("Se pulsó el botón cancelar");

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    private void establecerConexión() {
        if (!nameField.getText().isEmpty() && !ipField.getText().isEmpty() && !portField.getText().isEmpty()) {
            try (
            Socket socket = new Socket(ipField.getText(), Integer.valueOf(portField.getText()));
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
            ){
                String[] mensajes = {
                    "Hola, servidor.",
                    "¿Cómo estás?",
                    "Aquí un cliente enviando mensajes automáticos.",
                    "Mensaje final."
                };
    
                for (String mensaje : mensajes) {
                    // Enviar el mensaje al servidor
                    System.out.println("Tú: " + mensaje);
                    salida.println(mensaje);
    
                    // Leer la respuesta del servidor
                    String respuesta = entrada.readLine();
                    System.out.println("Servidor: " + respuesta);
                }
    
                // Enviar el mensaje de desconexión
                salida.println("salir");
                System.out.println("Cliente desconectado.");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    } 
}
