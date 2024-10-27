package com.client;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.scene.control.Button;
import org.json.JSONObject;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class ControllerPlay implements Initializable {

    @FXML
    private Canvas canvas;
    @FXML
    private Canvas attackCanvas;
    @FXML
    private Canvas defenseCanvas;
    @FXML
    private Button buttonReady = new Button();
    private GraphicsContext gc;
    private Boolean showFPS = false;
    private Boolean playingMatch = false;
    private double oldPositionX, oldPositionY;

    private PlayTimer animationTimer;
    private PlayGrid grid;

    public Map<String, JSONObject> clientMousePositions = new HashMap<>();
    private Boolean mouseDragging = false;
    private double mouseOffsetX, mouseOffsetY;

    public static Map<String, JSONObject> selectableObjects = new HashMap<>();
    private String selectedObject = "";

    public static ControllerPlay instance;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        instance = this;

        System.out.println("CARGO");

        this.gc = canvas.getGraphicsContext2D();
        
        // Establecer el tamaño del canvas fijo
        canvas.setWidth(550);
        canvas.setHeight(375);

        // Configurar los listeners
        // El tamaño del canvas no debe cambiar al redimensionar la ventana
        UtilsViews.parentContainer.heightProperty().addListener((observable, oldValue, newvalue) -> {});
        UtilsViews.parentContainer.widthProperty().addListener((observable, oldValue, newvalue) -> {});

        // Configurar los manejadores de eventos del ratón
        canvas.setOnMouseMoved(this::setOnMouseMoved);
        canvas.setOnMousePressed(this::onMousePressed);
        canvas.setOnMouseDragged(this::onMouseDragged);
        canvas.setOnMouseReleased(this::onMouseReleased);

        // Definir la rejilla
        grid = new PlayGrid(30, 30, 30, 10, 10);  // Asegúrate de que estos valores sean correctos para tu rejilla

        // Iniciar el temporizador de animación
        animationTimer = new PlayTimer(this::run, this::draw, 0);
        start();
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
            newPosition.put("col", -1);
            newPosition.put("row", -1);
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

            if (isPositionInsideObject(mouseX, mouseY, objX, objY,  cols, rows)) {
                selectedObject = objectId;
                System.out.println("Barco " + selectedObject + " clickeado2");
                mouseDragging = true;
                mouseOffsetX = event.getX() - objX;
                mouseOffsetY = event.getY() - objY;
                oldPositionX = objX;
                oldPositionY = objY;
                break;
            }
        }
    }

    private void onMouseDragged(MouseEvent event) {
        if (mouseDragging) {
            JSONObject obj = selectableObjects.get(selectedObject);
            double objX = event.getX() - mouseOffsetX;
            double objY = event.getY() - mouseOffsetY;
            
            obj.put("x", objX);
            obj.put("y", objY);
            obj.put("col", grid.getCol(objX));
            obj.put("row", grid.getRow(objY));

            JSONObject msgObj = selectableObjects.get(selectedObject);
            msgObj.put("type", "clientSelectableObjectMoving");
            msgObj.put("objectId", obj.getString("objectId"));
        
            if (ControllerConnect.clienteWebSocket != null) {
                ControllerConnect.clienteWebSocket.send(msgObj.toString());
            }
        }
        setOnMouseMoved(event);
    }

    private void onMouseReleased(MouseEvent event) {
        if (!selectedObject.isEmpty()) {
            JSONObject obj = selectableObjects.get(selectedObject);
            int objCol = obj.getInt("col");
            int objRow = obj.getInt("row");

            int objHeight = obj.getInt("rows");
            int objWide = obj.getInt("cols");

            int objX = obj.getInt("x");
            int objY = obj.getInt("y");


            double mouseX = event.getX();
            double mouseY = event.getY();

            int bottomHeight = objHeight + objCol - 1;
            int bottomWide = objWide + objRow - 1;

            if (objCol != -1 && objRow != -1) {
                int newPositionX = grid.getCellX(objCol);
                int newPositionY = grid.getCellY(objRow);

                if (oldPositionX != newPositionX || oldPositionY != newPositionY) {
                    // Si no esta en la misma posicion y no sobresale del grid
                    if (bottomHeight < 10 && bottomWide < 10) {
                        // Comprobar si no hay otro barco ocupando la misma posición
                        boolean canMove = true;
                        for (String objectId : selectableObjects.keySet()) {
                            JSONObject otherObj = selectableObjects.get(objectId);
                            if (!objectId.equals(selectedObject)) {
                                int otherObjX = otherObj.getInt("x");
                                int otherObjY = otherObj.getInt("y");
                                int otherObjCols = otherObj.getInt("cols");
                                int otherObjRows = otherObj.getInt("rows");

                                if (isPositionInsideObject(newPositionX, newPositionY, otherObjX, otherObjY, otherObjCols, otherObjRows) ||
                                    isPositionInsideObject(newPositionX, newPositionY, objX, objY, objWide, objHeight) ||
                                    isPositionInsideObject(objX, objY, otherObjX, otherObjY, otherObjCols, otherObjRows)) {
                                    canMove = false;
                                }
                            }
                        }
                        if (canMove) {
                            obj.put("x", newPositionX);
                            obj.put("y", newPositionY);
                            System.out.println("Se ha movido el barco");
                        } else {
                            // Return to old position if out of grid
                            obj.put("x", oldPositionX);
                            obj.put("y", oldPositionY);
                            System.out.println("No se puede poner un barco encima de otro");
                        }
                    } else {
                        System.out.println("Parte del barco sobresale de la cuadricula, no se puede girar.");
                    }
                } else {
                    if (bottomHeight < 10 && bottomWide < 10) {
                        System.out.println("Barco girado");
                        obj.put("x", newPositionX);
                        obj.put("y", newPositionY);
                        obj.put("cols", objHeight);
                        obj.put("rows", objWide);
                    } else {
                        System.out.println("Parte del barco sobresale de la cuadricula, no se puede girar.");
                    }
                }
            } else {
                // Return to old position if out of grid
                obj.put("x", oldPositionX);
                obj.put("y", oldPositionY);
                System.out.println("El barco está fuera de la cuadrícula y ha sido devuelto a su posición anterior");
            }

            JSONObject msgObj = selectableObjects.get(selectedObject);
            msgObj.put("type", "clientSelectableObjectMoving");
            msgObj.put("objectId", obj.getString("objectId"));

            if (ControllerConnect.clienteWebSocket != null) {
                ControllerConnect.clienteWebSocket.send(msgObj.toString());
            }

            mouseDragging = false;
            selectedObject = "";
        }
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

    public Boolean isPositionInsideObject(double positionX, double positionY, int objX, int objY, int cols, int rows) {
        double cellSize = grid.getCellSize();
        double objectWidth = cols * cellSize;
        double objectHeight = rows * cellSize;

        double objectRightX = objX + objectWidth;
        double objectBottomY = objY + objectHeight;

        return positionX >= (double) objX && positionX < objectRightX &&
               positionY >= (double) objY && positionY < objectBottomY;
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
    
        // Dibuja los círculos del ratón si es necesario
        if (playingMatch) {
            for (String clientId : clientMousePositions.keySet()) {
                JSONObject position = clientMousePositions.get(clientId);
                gc.setFill("A".equals(clientId) ? Color.BLUE : Color.GREEN);
                gc.fillOval(position.getInt("x") - 5, position.getInt("y") - 5, 10, 10);
            }
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
        double width = obj.getInt("cols") * cellSize;
        double height = obj.getInt("rows") * cellSize;

        // Seleccionar un color basat en l'objectId
        Color color;
        switch (objectId.toLowerCase()) {
            case "red":
                color = Color.RED;
                break;
            case "blue":
                color = Color.BLUE;
                break;
            case "green":
                color = Color.GREEN;
                break;
            case "yellow":
                color = Color.YELLOW;
                break;
            default:
                color = Color.GRAY;
                break;
        }

        // Dibuixar el rectangle
        gc.setFill(color);
        gc.fillRect(x, y, width, height);

        // Dibuixar el contorn
        gc.setStroke(Color.BLACK);
        gc.strokeRect(x, y, width, height);

        // Opcionalment, afegir text (per exemple, l'objectId)
        gc.setFill(Color.BLACK);
        gc.fillText(objectId, x + 5, y + 15);
    }

    public void playerReady(){
        if(playingMatch){
            playingMatch = false;
            buttonReady.setText("Ready");
        } else {
            playingMatch = true;
            buttonReady.setText("Not Ready");
        }
    }

    private void onAttackGridClicked(MouseEvent event) {
        double mouseX = event.getX();
        double mouseY = event.getY();

        if (grid.isPositionInsideGrid(mouseX, mouseY)) {
            int col = grid.getCol(mouseX);
            int row = grid.getRow(mouseY);

            // Send attack message to the server
            sendAttackMessage(col, row);
        }
    }

    private void sendAttackMessage(int col, int row) {
        JSONObject attackMessage = new JSONObject();
        attackMessage.put("type", "attack");
        attackMessage.put("col", col);
        attackMessage.put("row", row);
        attackMessage.put("clientId", ControllerConnect.nombre);

        if (ControllerConnect.clienteWebSocket != null) {
            ControllerConnect.clienteWebSocket.send(attackMessage.toString());
        }
    }
}