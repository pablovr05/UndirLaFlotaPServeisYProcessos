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
import javafx.scene.text.Text;

public class ControllerMatch implements Initializable {


    @FXML
    private Canvas attackCanvas;
    @FXML
    private Canvas defenseCanvas;
    @FXML
    private Text textTurn;
    private GraphicsContext gcAttack, gcDefense;

    private String enemyID;
    private String playerID;

    private PlayTimer animationTimer;
    private PlayGrid attackGrid, defenseGrid;

    public Map<String, JSONObject> clientMousePositions;

    private int highlightedCol = -1; // Columna de la celda seleccionada
    private int highlightedRow = -1; // Fila de la celda seleccionada

    public JSONObject selectableObjects;
    private String selectedObject = "";

    public static ControllerMatch instance;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        instance = this;

        clientMousePositions = new HashMap<>();

        selectableObjects = ControllerPlay.instance.getAllShipsAsJSON();

        // Get drawing context
        this.gcAttack = attackCanvas.getGraphicsContext2D();
        this.gcDefense = defenseCanvas.getGraphicsContext2D();

        // Set listeners
        UtilsViews.parentContainer.heightProperty().addListener((observable, oldValue, newvalue) -> { onSizeChanged(); });
        UtilsViews.parentContainer.widthProperty().addListener((observable, oldValue, newvalue) -> { onSizeChanged(); });

        attackCanvas.setOnMouseMoved(this::setOnMouseMoved);
        attackCanvas.setOnMousePressed(this::onMousePressed);
        attackCanvas.setOnMouseDragged(this::onMouseDragged);
        attackCanvas.setOnMouseReleased(this::onMouseReleased);

        // Define grids
        defenseGrid = new PlayGrid(30, 30, 20, 10, 10);
        attackGrid = new PlayGrid(30, 30, 20, 10, 10);

        // Start run/draw timer bucle
        animationTimer = new PlayTimer(this::run, this::draw, 0);
        start();
    }

    // When window changes its size
    public void onSizeChanged() {

        double width = UtilsViews.parentContainer.getWidth();
        double height = UtilsViews.parentContainer.getHeight();
        attackCanvas.setWidth(width);
        attackCanvas.setHeight(height);
        defenseCanvas.setWidth(width);
        defenseCanvas.setHeight(height);
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
        
        if (attackGrid.isPositionInsideGrid(mouseX, mouseY)) {
            highlightedCol = attackGrid.getCol(mouseX);
            highlightedRow = attackGrid.getRow(mouseY);
            newPosition.put("col", highlightedCol);
            newPosition.put("row", highlightedRow);
        } else {
            highlightedCol = -1;
            highlightedRow = -1;
            newPosition.put("col", -1);
            newPosition.put("row", -1);
        }
        
        newPosition.put("clientId", ControllerConnect.nombre);
        
        // Enviar la posición al servidor
        sendPositionToServer(newPosition);
        
        // Mensaje de depuración
        System.out.println("Posición de mouse del usuario enviada: X=" + mouseX + ", Y=" + mouseY);
    }
    
    

    private void onMousePressed(MouseEvent event) {

        double mouseX = event.getX();
        double mouseY = event.getY();

        selectedObject = "";

        for (String objectId : selectableObjects.keySet()) {
            JSONObject obj = (JSONObject) selectableObjects.get(objectId);
            int objX = obj.getInt("x");
            int objY = obj.getInt("y");
            int cols = obj.getInt("cols");
            int rows = obj.getInt("rows");

            if (isPositionInsideObject(mouseX, mouseY, objX, objY,  cols, rows)) {
                selectedObject = objectId;
                System.out.println("Barco " + selectedObject + " clickeado2");
                break;
            }
        }
    }

    private void onMouseDragged(MouseEvent event) {
        setOnMouseMoved(event);
    }

    private void onMouseReleased(MouseEvent event) {
        double mouseX = event.getX();
        double mouseY = event.getY();

        if (attackGrid.isPositionInsideGrid(mouseX, mouseY)) {

            sendAttackMessage(mouseX, mouseY);
        }
    }

    public void setPlayersMousePositions(JSONObject positions) {
        clientMousePositions.clear();
        for (String clientId : positions.keySet()) {
            JSONObject positionObject = positions.getJSONObject(clientId);
            clientMousePositions.put(clientId, positionObject);
        }
    }
    // Run game (and animations)
    private void run(double fps) {

        if (animationTimer.fps < 1) { return; }

        // Update objects and animations here
    }

    // Draw game to canvas
    public void draw() {
        // Limpiar el área de dibujo
        gcAttack.clearRect(0, 0, attackCanvas.getWidth(), attackCanvas.getHeight());
        gcDefense.clearRect(0, 0, defenseCanvas.getWidth(), defenseCanvas.getHeight());
    
        // Dibujar la celda resaltada en rosa
        if (highlightedCol >= 0 && highlightedRow >= 0) {
            gcAttack.setFill(Color.BLUE);
            gcAttack.fillRect(attackGrid.getCellX(highlightedCol), attackGrid.getCellY(highlightedRow), attackGrid.getCellSize(), attackGrid.getCellSize());
        }
    
        // Dibujar celdas coloreadas para el cursor dentro de las cuadrículas
        for (String clientId : clientMousePositions.keySet()) {
            JSONObject position = clientMousePositions.get(clientId);
    
            // Verifica que "col" y "row" existen antes de acceder
            if (position.has("col") && position.has("row")) {
                int col = position.getInt("col");
                int row = position.getInt("row");
    
                // Comprobar si la posición está dentro de los límites de la cuadrícula
                if (row >= 0 && col >= 0) {
                    if ("A".equals(clientId)) { // Cliente local
                        gcAttack.setFill(Color.LIGHTBLUE); // Color para la cuadrícula de ataque del usuario
                        gcAttack.fillRect(attackGrid.getCellX(col), attackGrid.getCellY(row), attackGrid.getCellSize(), attackGrid.getCellSize());
                    } else { // Cliente enemigo
                        gcDefense.setFill(Color.PINK); // Color para la cuadrícula de defensa del enemigo
                        gcDefense.fillRect(defenseGrid.getCellX(col), defenseGrid.getCellY(row), defenseGrid.getCellSize(), defenseGrid.getCellSize());
                    }
                }
            }
        }

        // Dibujar las cuadrículas
        drawGrid();

        // Dibujar objetos seleccionables
        for (String objectId : selectableObjects.keySet()) {
            JSONObject selectableObject = (JSONObject) selectableObjects.get(objectId);
            drawSelectableObject(objectId, selectableObject);
        }
    
        // Dibujar el cursor en cada cuadrícula
        for (String clientId : clientMousePositions.keySet()) {
            JSONObject position = clientMousePositions.get(clientId);
            double mouseX = position.getDouble("x");
            double mouseY = position.getDouble("y");
    
            // Seleccionar el contexto y color en función del cliente
            if ("A".equals(clientId)) { // Usuario local
                gcAttack.setFill(Color.BLUE); // Color para el cursor del usuario local
                gcAttack.fillOval(mouseX - 5, mouseY - 5, 10, 10);
            } else { // Enemigo
                gcDefense.setFill(Color.RED); // Color para el cursor del enemigo
                gcDefense.fillOval(mouseX - 5, mouseY - 5, 10, 10);
            }
        }

    }
    
    
    
    
    public void updateGrid(int col, int row, Color color, PlayGrid grid, GraphicsContext gc) {
        double cellSize = grid.getCellSize();
        double x = grid.getStartX() + col * cellSize;
        double y = grid.getStartY() + row * cellSize;

        gc.setFill(color);
        gc.fillRect(x, y, cellSize, cellSize);
    }

    public void drawGrid() {
        gcDefense.setStroke(Color.DARKRED);
        drawGrids(defenseGrid, gcDefense);

        gcAttack.setStroke(Color.DARKBLUE);
        drawGrids(attackGrid, gcAttack);
    }

    static void drawGrids(PlayGrid grid, GraphicsContext gc) {
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
                    gc.setFont(javafx.scene.text.Font.font(12));
                    gc.fillText(colLetter, x + cellSize / 2 - 5, y - 5);
                }

                // Draw row numbers
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
        double cellSize = defenseGrid.getCellSize();
    
        System.out.println("Object ID: " + objectId);
        System.out.println("Object Data: " + obj);
    
        // Determine the position based on available keys
        double x, y;
    
        if (obj.has("col") && obj.has("row")) {
            // Use "col" and "row" for positioning if they exist
            int col = obj.getInt("col");
            int row = obj.getInt("row");
            x = defenseGrid.getStartX() + col * cellSize;
            y = defenseGrid.getStartY() + row * cellSize;
        } else if (obj.has("x") && obj.has("y")) {
            // Fall back to "x" and "y" if "col" and "row" are missing
            x = obj.getDouble("x");
            y = obj.getDouble("y");
        } else {
            System.out.println("Error: Missing positioning keys for object " + objectId);
            return;
        }
    
        // Get the size using "cols" and "rows" (with defaults if missing)
        double width = obj.optInt("cols", 1) * cellSize;
        double height = obj.optInt("rows", 1) * cellSize;

        if (!obj.getBoolean("isVertical")) {
            System.out.println("ES HORIZONTAL");
                width = height;
                height = cellSize;
        } else {
            System.out.println("ES VERTICAL");
        }
    
        // Select a color based on the objectId
        Color color = switch (objectId.toLowerCase()) {
            case "red" -> Color.RED;
            case "blue" -> Color.BLUE;
            case "green" -> Color.GREEN;
            case "yellow" -> Color.YELLOW;
            default -> Color.GRAY;
        };
    
        // Aplicar conversores para escalar la separación de 30pixeles a 20px
        for (double i = x; i > 1; i -= 30) {
            if (i != 30) {
                x -= 10;
            }
        }
    
        for (double i = y; i > 1; i -= 30) {
            if (i != 30) {
                y -= 10;
            }
        }
    
        System.out.println("(" + x + "," + y + ")" + width + " " + height);
    
        // Draw the rectangle
        gcDefense.setFill(color);
        gcDefense.fillRect(x, y, width, height);
    
        // Draw the outline
        gcDefense.setStroke(Color.BLACK);
        gcDefense.strokeRect(x, y, width, height);
    }
    
    private void sendAttackMessage(double mouseX, double mouseY) {
        JSONObject attackMessage = new JSONObject();
        double cellSize = attackGrid.getCellSize();

        attackMessage.put("type", "attack");
        attackMessage.put("mouseX", mouseX);
        attackMessage.put("mouseY", mouseY);
        attackMessage.put("cellsize", cellSize);

        if (ControllerConnect.clienteWebSocket != null) {
            ControllerConnect.clienteWebSocket.send(attackMessage.toString());
        }
    }

    private boolean isPositionInsideObject(double mouseX, double mouseY, int objX, int objY, int cols, int rows) {
        // Obtener el tamaño de la celda del grid
        double cellSize = attackGrid.getCellSize(); // Asumiendo que estás usando el grid de ataque
    
        // Calcular las coordenadas del objeto
        double objectX = objX * cellSize; // Coordenada X inicial del objeto
        double objectY = objY * cellSize; // Coordenada Y inicial del objeto
    
        // Calcular las coordenadas del objeto en base a su tamaño
        double objectWidth = cols * cellSize; // Ancho del objeto
        double objectHeight = rows * cellSize; // Altura del objeto
    
        // Comprobar si la posición del mouse está dentro de las coordenadas del objeto
        return mouseX >= objectX && mouseX <= (objectX + objectWidth) &&
               mouseY >= objectY && mouseY <= (objectY + objectHeight);
    }

    private void sendPositionToServer(JSONObject position) {
        // Aquí puedes enviar la posición al servidor a través de tu cliente WebSocket
        if (ControllerConnect.clienteWebSocket != null) {
            position.put("type", "mouseMoved"); // Indica el tipo de mensaje
            ControllerConnect.clienteWebSocket.send(position.toString());
        }
    }

    public static void updateCursorPosition(double mouseX, double mouseY, String clientId) {
        JSONObject position = new JSONObject();
        position.put("x", mouseX);
        position.put("y", mouseY);
        position.put("clientId", clientId);

        // Actualizar la posición en el mapa
        instance.clientMousePositions.put(clientId, position);
    }
}
