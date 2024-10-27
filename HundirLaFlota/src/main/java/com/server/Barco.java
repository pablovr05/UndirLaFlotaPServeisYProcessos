package com.server;

import org.json.JSONObject;

public class Barco {
    private String nombre;
    private int tamaño;
    private int fila;
    private int columna;
    private boolean esHorizontal;

    public Barco(JSONObject jsonBarco) {
        this.nombre = jsonBarco.getString("nombre");
        this.tamaño = jsonBarco.getInt("tamaño");
        this.fila = jsonBarco.getInt("fila");
        this.columna = jsonBarco.getInt("columna");
        this.esHorizontal = jsonBarco.getBoolean("esHorizontal");
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

    public void setEsHorizontal(boolean esHorizontal) {
        this.esHorizontal = esHorizontal;
    }

    public boolean estaHundido(int[][] grid) {
        for (int i = 0; i < tamaño; i++) {
            int filaActual = fila;
            int columnaActual = columna;

            if (esHorizontal) {
                columnaActual += i;
            } else {
                filaActual += i;
            }

            // Verificamos si cada parte del barco ha sido golpeada (valor 2)
            if (grid[filaActual][columnaActual] != 2) {
                return false; // Si alguna parte del barco no está golpeada, no está hundido
            }
        }
        return true; // Todas las partes del barco fueron golpeadas
    }
}
