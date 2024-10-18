package com.project;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class ControllerGame {

    private String nombre;

    private BufferedReader entrada;

    private PrintWriter salida;

    public ControllerGame() {

    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setSalidaEntrada(PrintWriter salida, BufferedReader entrada) {
        this.salida = salida;
        this.entrada = entrada;
    }
    
}
