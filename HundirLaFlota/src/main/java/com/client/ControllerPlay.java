package com.client;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.scene.control.Button;
import org.json.JSONObject;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;


import org.json.JSONArray;
import org.json.JSONObject;

public class ControllerPlay implements Initializable {

    @FXML
    private Canvas canvas;
    @FXML
    private Button buttonReady = new Button();
    @FXML
    private Pane overlayPane;

    private GraphicsContext gc;
    private Boolean showFPS = false;
    private Boolean playingMatch = false;

    private PlayTimer animationTimer;
    private PlayGrid grid;

    public Map<String, JSONObject> clientMousePositions = new HashMap<>();
    private Boolean mouseDragging = false;

    public static Map<String, JSONObject> selectableObjects = new HashMap<>();
    private String selectedObject = "";

    private Map<String, List<int[]>> occupiedPositions = new HashMap<>();
    private Map<String, double[]> boatPositions = new HashMap<>();

    public static ControllerPlay instance;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;

        this.gc = canvas.getGraphicsContext2D();
        
        // Establecer el tamaño del canvas fijo
        canvas.setWidth(550);
        canvas.setHeight(375);

        boatPositions.put("00", new double[]{450, 20});
        boatPositions.put("01", new double[]{410, 20});
        boatPositions.put("02", new double[]{450, 125});
        boatPositions.put("03", new double[]{410, 125});

        removeOverlay();

        // Configurar el evento de clic para que el canvas obtenga el foco
        canvas.setOnMouseClicked(event -> canvas.requestFocus());

        // Configurar los listeners
        // El tamaño del canvas no debe cambiar al redimensionar la ventana
        UtilsViews.parentContainer.heightProperty().addListener((observable, oldValue, newvalue) -> {});
        UtilsViews.parentContainer.widthProperty().addListener((observable, oldValue, newvalue) -> {});

        // Configurar los manejadores de eventos del ratón
        canvas.setOnMouseMoved(this::setOnMouseMoved);
        canvas.setOnMousePressed(this::onMousePressed);
        canvas.setOnMouseDragged(this::onMouseDragged);
        canvas.setOnMouseReleased(this::onMouseReleased);
        
        // Configurar el manejador de eventos de teclado para rotar el objeto
        canvas.setOnKeyPressed(this::onKeyPressed);

        // Definir la rejilla
        grid = new PlayGrid(30, 30, 30, 10, 10);  // Asegúrate de que estos valores sean correctos para tu rejilla

        // Iniciar el temporizador de animación
        animationTimer = new PlayTimer(this::run, this::draw, 0);
        start();
    }

    private void onKeyPressed(KeyEvent event) {
        if (!selectedObject.isEmpty() && event.getCode() == KeyCode.R) {
            // Alternar la orientación
            JSONObject obj = selectableObjects.get(selectedObject);
            boolean isVertical = obj.optBoolean("isVertical", true); // Valor predeterminado: true (horizontal)
            obj.put("isVertical", !isVertical); // Cambiar la orientación
            System.out.println(selectedObject + " ahora es " + (isVertical ? "vertical" : "horizontal"));
        }
    }


    // When window changes its size
    public void onSizeChanged() {

        double width = UtilsViews.parentContainer.getWidth();
        double height = UtilsViews.parentContainer.getHeight();
        canvas.setWidth(width);
        canvas.setHeight(height);
    }

    // Start animation timer
    public void start() {
        animationTimer.start();
    }

    // Stop animation timer
    public void stop() {
        animationTimer.stop();
    }

    private void setOnMouseMoved(MouseEvent event) {
        double mouseX = event.getX();
        double mouseY = event.getY();

        JSONObject newPosition = new JSONObject();
        newPosition.put("x", mouseX);
        newPosition.put("y", mouseY);
        if (grid.isPositionInsideGrid(mouseX, mouseY)) {                
            newPosition.put("col", grid.getCol(mouseX));
            newPosition.put("row", grid.getRow(mouseY));
        } else {
            newPosition.put("col", 2);
            newPosition.put("row", 2);
        }

        clientMousePositions.put(ControllerConnect.nombre, newPosition);

        JSONObject msgObj = clientMousePositions.get(ControllerConnect.nombre);
        msgObj.put("type", "clientMouseMoving");
        msgObj.put("clientId", ControllerConnect.nombre);
    
        if (ControllerConnect.clienteWebSocket != null) {
            ControllerConnect.clienteWebSocket.send(msgObj.toString());
        }
    }

    private void onMousePressed(MouseEvent event) {
        double mouseX = event.getX();
        double mouseY = event.getY();
    
        selectedObject = "";
        mouseDragging = false;
    
        for (String objectId : selectableObjects.keySet()) {
            JSONObject obj = selectableObjects.get(objectId);
            int objX = obj.getInt("x");
            int objY = obj.getInt("y");
            int cols = obj.getInt("cols");
            int rows = obj.getInt("rows");
            boolean isVertical = obj.optBoolean("isVertical", true);
    
            if (isPositionInsideObject(mouseX, mouseY, objX, objY, cols, rows, obj)) {
                selectedObject = objectId;
                System.out.println("Barco " + selectedObject + " clickeado");
                mouseDragging = true;
                break;
            }
        }
    }
    

    private void onMouseDragged(MouseEvent event) {
        if (mouseDragging) {
            // Actualizar temporalmente la posición del barco con las coordenadas del ratón
            JSONObject obj = selectableObjects.get(selectedObject);
            obj.put("x", event.getX());
            obj.put("y", event.getY());
        }
    }

    private void onMouseReleased(MouseEvent event) {
    if (!selectedObject.isEmpty()) {
        JSONObject obj = selectableObjects.get(selectedObject);
        
        // Obtener el tamaño del barco en celdas
        int cols = obj.getInt("cols");
        int rows = obj.getInt("rows");

        // Verificar la orientación
        boolean isVertical = obj.optBoolean("isVertical", true);
        if (!isVertical) {
            // Intercambiar columnas y filas si es vertical
            int temp = cols;
            cols = rows;
            rows = temp;
        }

        // Obtener la posición de la esquina superior izquierda del barco en píxeles
        double mouseX = event.getX();
        double mouseY = event.getY();

        // Obtener la columna y fila de la cuadrícula donde se colocará la esquina superior izquierda del barco
        int startCol = grid.getCol(mouseX);
        int startRow = grid.getRow(mouseY);

        // Verificar si el barco cabe dentro de los límites de la cuadrícula
        if (startCol >= 0 && startRow >= 0 && startCol + cols <= grid.getCols() && startRow + rows <= grid.getRows()) {
            // Verificar si el barco colisiona con otros barcos
            
            if (!checkCollision(startCol, startRow, cols, rows, isVertical)) {
                // Actualizar la posición del barco en píxeles a la cuadrícula (esquina superior izquierda)
                obj.put("x", grid.getCellX(startCol));
                obj.put("y", grid.getCellY(startRow));

                // Almacenar las posiciones ocupadas por este barco
                List<int[]> positions = new ArrayList<>();
                for (int row = startRow; row < startRow + rows; row++) {
                    for (int col = startCol; col < startCol + cols; col++) {
                        positions.add(new int[]{col, row});
                    }
                }
                occupiedPositions.put(selectedObject, positions);

                // Imprimir todas las celdas que el barco ocupa
                System.out.println("Celdas ocupadas por el barco:");
                for (int[] pos : positions) {
                    System.out.println("Celda: (" + pos[0] + ", " + pos[1] + ")");
                }
            } else {
                obj.put("isVertical", true); // Asegúrate de que se dibuje verticalmente
                occupiedPositions.remove(selectedObject); // Borrar posiciones ocupadas
                double[] newPosition = boatPositions.getOrDefault(selectedObject, new double[]{200, 200}); // Posición por defecto
                obj.put("x", newPosition[0]);
                obj.put("y", newPosition[1]);
                System.out.println("El barco " + selectedObject + " no se puede colocar en esa posición y ha sido movido a (" + newPosition[0] + ", " + newPosition[1] + ").");
            }
        } else {
            obj.put("isVertical", true); // Asegúrate de que se dibuje verticalmente
            occupiedPositions.remove(selectedObject); // Borrar posiciones ocupadas
            double[] newPosition = boatPositions.getOrDefault(selectedObject, new double[]{200, 200}); // Posición por defecto
            obj.put("x", newPosition[0]);
            obj.put("y", newPosition[1]);
            System.out.println("El barco " + selectedObject + " no se puede colocar en esa posición y ha sido movido a (" + newPosition[0] + ", " + newPosition[1] + ").");
        }

        // Resetear selección y arrastre
        mouseDragging = false;
        selectedObject = "";
        }
    }
    
    private boolean checkCollision(int startCol, int startRow, int cols, int rows, boolean isVertical) {

        System.out.println(occupiedPositions);

        // Iterar sobre todas las posiciones ocupadas por otros barcos
        for (String objectId : occupiedPositions.keySet()) {
            if (objectId.equals(selectedObject)) {
                continue; // Ignorar el barco actualmente seleccionado
            }
    
            List<int[]> positions = occupiedPositions.get(objectId);
            System.out.println("Barco " + objectId + " ocupa las siguientes posiciones:");
    
            // Imprimir las posiciones ocupadas
            for (int[] pos : positions) {
                int objCol = pos[0];
                int objRow = pos[1];
                System.out.printf("Posición ocupada: (%d, %d)%n", objCol, objRow);
            }

            int intHPlus = 0;
            int intVPlus = 0;

            int checkCol = 0;
            int checkRow = 0;

    
            // Comprobar todas las celdas que ocupará el nuevo barco
            for (int colOffset = 0; colOffset < cols; colOffset++) {
                for (int rowOffset = 0; rowOffset < rows; rowOffset++) {
                    // Calcular la posición que ocupa el nuevo barco
                    checkCol = isVertical ? startCol + colOffset : startCol; // Columna a comprobar
                    checkRow = isVertical ? startRow : startRow + rowOffset; // Fila a comprobar

                    if (isVertical) {

                        checkRow = checkRow + intHPlus;
                        intHPlus++;

                    } else if (!isVertical) {
                        checkCol = checkCol + intVPlus;
                        intVPlus++;
                    }
    
                    // Imprimir la posición del nuevo barco que se está comparando
                    System.out.printf("Comparando posición del nuevo barco: (%d, %d)%n", checkCol, checkRow);
    
                    // Comparar con cada posición ocupada por otros barcos
                    for (int[] pos : positions) {
                        int objCol = pos[0];
                        int objRow = pos[1];
    
                        // Imprimir cada comparación con las posiciones ocupadas
                        System.out.printf("Comparando con posición ocupada: (%d, %d)%n", objCol, objRow);
    
                        // Verificar colisión
                        if (isVertical) {
                            // Comprobar si hay colisión horizontal
                            if (checkRow == objRow && checkCol >= objCol && checkCol < objCol + cols) {
                                return true; // Hay colisión
                            }
                        } else {
                            // Comprobar si hay colisión vertical
                            if (checkCol == objCol && checkRow >= objRow && checkRow < objRow + rows) {
                                return true; // Hay colisión
                            }
                        }
                    }
                }
            }
        }
        return false; // No hay colisión
    }
    
    public void setPlayersMousePositions(JSONObject positions) {
        clientMousePositions.clear();
        for (String clientId : positions.keySet()) {
            JSONObject positionObject = positions.getJSONObject(clientId);
            clientMousePositions.put(clientId, positionObject);
        }
    }

    public void setSelectableObjects(JSONObject objects) {
        selectableObjects.clear();
        for (String objectId : objects.keySet()) {
            JSONObject positionObject = objects.getJSONObject(objectId);
            selectableObjects.put(objectId, positionObject);
        }
    }

    public Map<String, JSONObject> getSelectableObjects() {
        return selectableObjects;
    }

    public Boolean isPositionInsideObject(double positionX, double positionY, int objX, int objY, int cols, int rows, JSONObject obj) {
        double cellSize = grid.getCellSize();
        boolean isVertical = obj.optBoolean("isVertical", true); // Obtener la orientación del objeto
        double objectWidth = isVertical ? cols * cellSize : rows * cellSize; // Ajustar el ancho según la orientación
        double objectHeight = isVertical ? rows * cellSize : cols * cellSize; // Ajustar la altura según la orientación
    
        double objectRightX = objX + objectWidth;
        double objectBottomY = objY + objectHeight;
    
        return positionX >= objX && positionX < objectRightX &&
               positionY >= objY && positionY < objectBottomY;
    }
    
    

    // Run game (and animations)
    private void run(double fps) {

        if (animationTimer.fps < 1) { return; }

        // Update objects and animations here
    }

    // Draw game to canvas
    public void draw() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight()); // Limpia el canvas
    
        // Dibuja la rejilla
        drawGrid();
    
        // Dibuja los objetos seleccionables
        for (String objectId : selectableObjects.keySet()) {
            JSONObject selectableObject = selectableObjects.get(objectId);
            drawSelectableObject(objectId, selectableObject);
        }
    
        // Dibuja FPS si es necesario
        if (showFPS) {
            animationTimer.drawFPS(gc);
        }
    }
    

    public void drawGrid() {
        gc.setStroke(Color.BLACK);
    
        for (int row = 0; row < grid.getRows(); row++) {
            for (int col = 0; col < grid.getCols(); col++) {
                double cellSize = grid.getCellSize();
                double x = grid.getStartX() + col * cellSize;
                double y = grid.getStartY() + row * cellSize;
    
                gc.strokeRect(x, y, cellSize, cellSize);
    
                // Dibuja las letras de las columnas y los números de las filas
                if (row == 0) {
                    String colLetter = String.valueOf((char) ('A' + col));
                    gc.setFill(Color.BLACK);
                    gc.setFont(javafx.scene.text.Font.font(12));
                    gc.fillText(colLetter, x + cellSize / 2 - 5, y - 5);
                }
    
                if (col == 0) {
                    String rowNumber = String.valueOf(row + 1);
                    gc.setFill(Color.BLACK);
                    gc.setFont(javafx.scene.text.Font.font(12));
                    gc.fillText(rowNumber, x - 15, y + cellSize / 2 + 5);
                }
            }
        }
    }
    

    public void drawSelectableObject(String objectId, JSONObject obj) {
        double cellSize = grid.getCellSize();
    
        int x = obj.getInt("x");
        int y = obj.getInt("y");
        int cols = obj.getInt("cols");
        int rows = obj.getInt("rows");
    
        // Verificar la orientación
        boolean isVertical = obj.optBoolean("isVertical", true);
        if (!isVertical) {
            // Intercambiar el ancho y alto si es vertical
            int temp = cols;
            cols = rows;
            rows = temp;
        }
    
        double width = cols * cellSize;
        double height = rows * cellSize;
    
        // Rotación del objeto
        int rotation = obj.optInt("rotation", 0); // Puedes omitir esto si no necesitas una rotación
        gc.save(); // Guarda el estado actual del contexto gráfico
        gc.translate(x + width / 2, y + height / 2); // Mueve el origen al centro del objeto
        gc.rotate(rotation); // Aplica la rotación si es necesaria
        gc.translate(-width / 2, -height / 2); // Mueve de nuevo el origen al ángulo original
    
        // Seleccionar un color basado en el objectId
        Color color = switch (objectId.toLowerCase()) {
            case "red" -> Color.RED;
            case "blue" -> Color.BLUE;
            case "green" -> Color.GREEN;
            case "yellow" -> Color.YELLOW;
            default -> Color.GRAY;
        };

        // Dibujar el rectángulo
        gc.setFill(color);
        gc.fillRect(0, 0, width, height);
    
        // Dibujar el contorno
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0, 0, width, height);
    
        // Opcionalmente, agregar texto (por ejemplo, el objectId)
        gc.setFill(Color.BLACK);
        gc.fillText(objectId, 5, 15);
    
        gc.restore(); // Restaura el contexto gráfico
    }

    public boolean allShipsPlaced() {
        // Recorremos todos los barcos en selectableObjects
        for (String objectId : selectableObjects.keySet()) {
            // Verificamos si el barco tiene posiciones en occupiedPositions
            if (!occupiedPositions.containsKey(objectId) || occupiedPositions.get(objectId).isEmpty()) {
                // Si algún barco no tiene posiciones, no todos los barcos están colocados
                return false;
            }
        }
        // Si todos los barcos tienen posiciones, todos los barcos están colocados
        return true;
    }
    

    public void playerReady(){
        if(playingMatch){
            playingMatch = false;
            buttonReady.setText("Ready");
            removeOverlay();
            ControllerConnect.instance.sendMessage("{\"type\":\"playerReady\",\"socketId\":\"" + ControllerConnect.clienteWebSocket + "\",\"name\":\"" + ControllerConnect.nombre + "\",\"enemyName\":\"" + null + "\"}");
        } else {
            if (allShipsPlaced()) {
                playingMatch = true;
                buttonReady.setText("Not Ready");
                createOverlay();
                ControllerConnect.instance.sendMessage("{\"type\":\"playerReady\",\"socketId\":\"" + ControllerConnect.clienteWebSocket + "\",\"name\":\"" + ControllerConnect.nombre + "\",\"enemyName\":\"" + ControllerMatchmaking.enemyName + "\"}");   
            } else {
                System.out.println("Debes tener todos los barcos puestos");
            }
        }
    }

    private void createOverlay() {
        overlayPane.setMouseTransparent(false); // Habilitar el pane superpuesto para capturar eventos
        overlayPane.setVisible(true); // Hacerlo visible

    }

    private void removeOverlay() {
        overlayPane.setVisible(false); // Ocultar el pane
    }

    public JSONObject getAllShipsAsJSON() {
        JSONObject allShipsJSON = new JSONObject();
        
        for (String objectId : selectableObjects.keySet()) {
            JSONObject shipInfo = new JSONObject();
            JSONObject obj = selectableObjects.get(objectId);
            
            // Extrae y almacena los atributos relevantes del barco
            shipInfo.put("x", obj.getInt("x"));
            shipInfo.put("y", obj.getInt("y"));
            shipInfo.put("cols", obj.getInt("cols"));
            shipInfo.put("rows", obj.getInt("rows"));
            shipInfo.put("isVertical", obj.optBoolean("isVertical", true));

            // Extrae las posiciones ocupadas por el barco en la cuadrícula
            JSONArray occupiedCells = new JSONArray();
            if (occupiedPositions.containsKey(objectId)) {
                for (int[] position : occupiedPositions.get(objectId)) {
                    JSONObject cell = new JSONObject();
                    cell.put("col", position[0]);
                    cell.put("row", position[1]);
                    occupiedCells.put(cell);
                }
            }

            shipInfo.put("occupiedCells", occupiedCells);
            allShipsJSON.put(objectId, shipInfo);
        }

        return allShipsJSON;
    }
}