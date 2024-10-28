package com.server;

import org.json.JSONArray;
import org.json.JSONObject;

public class Barco {
    private String nombre; // Nombre del barco (clave del JSON)
    private int tamaño;    // Tamaño del barco basado en occupiedCells
    private int fila;      // Coordenada fila
    private int columna;   // Coordenada columna
    private boolean esHorizontal; // Indicador de orientación del barco

    public Barco(String nombre, JSONObject jsonBarco) {
        this.nombre = nombre; // Guardar la clave como nombre
        this.tamaño = jsonBarco.getJSONArray("occupiedCells").length(); // Número de celdas ocupadas
        this.esHorizontal = !jsonBarco.getBoolean("isVertical"); // Se obtiene de "isVertical"

        // Obtener la primera posición ocupada para establecer fila y columna
        JSONObject primeraCelda = jsonBarco.getJSONArray("occupiedCells").getJSONObject(0);
        this.fila = primeraCelda.getInt("row");
        this.columna = primeraCelda.getInt("col");
    }

    public String getNombre() {
        return nombre;
    }

    public int getTamaño() {
        return tamaño;
    }

    public int getFila() {
        return fila;
    }

    public int getColumna() {
        return columna;
    }

    public boolean esHorizontal() {
        return esHorizontal;
    }

    public boolean estaHundido(int[][] grid) {
        for (int i = 0; i < tamaño; i++) {
            int filaActual = fila;
            int columnaActual = columna;

            if (esHorizontal) {
                columnaActual += i; // Si es horizontal, incrementa la columna
            } else {
                filaActual += i; // Si es vertical, incrementa la fila
            }

            // Verificamos si cada parte del barco ha sido golpeada (valor 2)
            if (grid[filaActual][columnaActual] != 2) {
                return false; // Si alguna parte del barco no está golpeada, no está hundido
            }
        }
        return true; // Todas las partes del barco fueron golpeadas
    }

    @Override
    public String toString() {
        return "Barco: " + nombre + " Tamaño: " + tamaño + " Fila: " + fila + " Columna: " + columna + " EsHorizontal: " + esHorizontal;
    }
}
