package com.server;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Tablero {
    private int[][] grid;
    private List<Barco> barcos;

    public Tablero(int tamaño) {
        this.grid = new int[tamaño][tamaño];
        this.barcos = new ArrayList<>();
    }

    public void cargarBarcosDesdeJSON(JSONObject jsonTablero) {
        // Iterar sobre las claves del objeto JSON para crear los barcos
        for (String clave : jsonTablero.keySet()) {
            JSONObject jsonBarco = jsonTablero.getJSONObject(clave);
            Barco barco = new Barco(clave, jsonBarco); // Pasar la clave como nombre
    
            barcos.add(barco);
            colocarBarcoEnTablero(barco);
        }
    }

    private void colocarBarcoEnTablero(Barco barco) {
        int fila = barco.getFila();
        int columna = barco.getColumna();

        for (int i = 0; i < barco.getTamaño(); i++) {
            if (barco.esHorizontal()) {
                grid[fila][columna + i] = 1; // Marcar la celda como ocupada por un barco
            } else {
                grid[fila + i][columna] = 1; // Marcar la celda como ocupada por un barco
            }
        }
    }

    public boolean descubrirCelda(int fila, int columna) {
        // Validar si la celda está dentro de los límites del tablero
        if (fila < 0 || fila >= grid.length || columna < 0 || columna >= grid[0].length) {
            System.out.println("Coordenadas fuera de los límites del tablero.");
            return false;
        }
    
        // Verificar si la celda ya fue descubierta
        if (grid[fila][columna] == 2 || grid[fila][columna] == -1) {
            System.out.println("Esta celda ya ha sido descubierta.");
            return false;
        }
    
        // Comprobar si la celda contiene parte de un barco (1 significa barco)
        if (grid[fila][columna] == 1) {
            grid[fila][columna] = 2; // Marcar la celda como descubierta con un golpe (2)
            System.out.println("¡Golpe en un barco!");
            return true; // Retorna true para indicar un golpe
        } else {
            grid[fila][columna] = -1; // Marcar la celda como descubierta sin golpe (-1)
            System.out.println("Celda vacía descubierta.");
            return false; // Retorna false para indicar que la celda estaba vacía
        }
    }

    public boolean batallaPerdida() {
        for (Barco barco : barcos) {
            if (!barco.estaHundido(grid)) { // Verificamos cada barco en el tablero
                return false; // Si encontramos un barco que no está hundido, no hemos perdido
            }
        }
        return true; // Todos los barcos están hundidos, entonces la batalla está perdida
    }

    public void mostrarTablero() {
        for (int[] fila : grid) {
            for (int celda : fila) {
                System.out.print(celda + " ");
            }
            System.out.println();
        }
    }
}
