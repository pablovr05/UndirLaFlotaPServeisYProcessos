package com.project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;


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
    private void acceptButton(ActionEvent event) {
        System.out.println("Se pultó el botón aceptar");
        establecerConexión();
    }

    @FXML
    private void cancelButton(ActionEvent event) {
        System.out.println("Se pulsó el botón cancelar");

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    private void establecerConexión() {
        if (!nameField.getText().isEmpty() && !ipField.getText().isEmpty() && !portField.getText().isEmpty()) {
            try {
                Socket socket = new Socket("localhost", 12345);
                // Crear fluxos per llegir i escriure dades
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

                // Enviar missatge al servidor
                output.println("Hola des del client!");

                // Llegir resposta del servidor
                String respostaServidor = input.readLine();
                System.out.println("\n---------------------------------------------");
                System.out.println("Resposta del servidor: " + respostaServidor);
                System.out.println("---------------------------------------------\n");

                // Tancar connexions
                input.close();
                output.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    } 
}
