package com.project;

public class Client {
    
}

/* 
En server:

        - Control de turnos
        - Timer de turnos
        - Timer para preparacion de partida - colocar barcos
        - eliminar la opcion de ver el puntero del rival en la preparacion
        - detectar si el una vez se ha posicionado el barco, que esten dentro del tablero
    
        - girar barco en mouseReleased
            int objHeight = obj.getInt("rows");
            int objWide = obj.getInt("cols");
            int oldPositionX = obj.getInt("x");
            int oldPositionY = obj.getInt("y");

            
        - dibujar letras y numeros en num / col CtrlPlay
            public void drawGrid() {
        gc.setStroke(Color.BLACK);

        for (int row = 0; row < grid.getRows(); row++) {
            for (int col = 0; col < grid.getCols(); col++) {
            double cellSize = grid.getCellSize();
            double x = grid.getStartX() + col * cellSize;
            double y = grid.getStartY() + row * cellSize;
            gc.strokeRect(x, y, cellSize, cellSize);

            // Draw column letters
            if (row == 0) {
                String colLetter = String.valueOf((char) ('A' + col));
                gc.setFill(Color.BLACK);
                gc.fillText(colLetter, x + cellSize / 2 - 5, y - 5);
            }

            // Draw row numbers
            if (col == 0) {
                String rowNumber = String.valueOf(row + 1);
                gc.setFill(Color.BLACK);
                gc.fillText(rowNumber, x - 15, y + cellSize / 2 + 5);
            }
            }
        }
    }


        String name2 = "02";
        JSONObject obj2 = new JSONObject();
        obj2.put("objectId", name2);
        obj2.put("x", 100);
        obj2.put("y", 150);
        obj2.put("cols", 1);
        obj2.put("rows", 4);
        selectableObjects.put(name2, obj2);

        String name3 = "03";
        JSONObject obj3 = new JSONObject();
        obj3.put("objectId", name3);
        obj3.put("x", 300); // X - Y posicion dibujo inicial
        obj3.put("y", 200);
        obj3.put("cols", 1);  // Girar cols y rows para girar el barco
        obj3.put("rows", 6);
        selectableObjects.put(name3, obj3);

En Controlador de juego:
    En mouse clicked:
        - getX y getY -> intercambiar al clickear para girar
        - añadir boton de finalizar la preparacion de partida
        - timer para cada turno?

*/
