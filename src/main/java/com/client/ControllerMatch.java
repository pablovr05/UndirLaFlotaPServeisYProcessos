package com.client;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.json.JSONObject;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class ControllerMatch implements Initializable {

    @FXML
    private Canvas attackCanvas;
    @FXML
    private Canvas defenseCanvas;
    @FXML
    public Text textTurn;
    @FXML
    private Pane overlayPane;
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

    private Boolean[][] userPaintBoard;
    private Boolean[][] enemyPaintBoard;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        instance = this;

        userPaintBoard = new Boolean[10][10];
        enemyPaintBoard = new Boolean[10][10];

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                userPaintBoard[i][j] = null;
                enemyPaintBoard[i][j] = null;
            }
        }

        clientMousePositions = new HashMap<>();

        selectableObjects = ControllerPlay.instance.getAllShipsAsJSON();

        playerID = ControllerConnect.nombre;
        enemyID = ControllerConnect.enemyName;
        // Get drawing context
        this.gcAttack = attackCanvas.getGraphicsContext2D();
        this.gcDefense = defenseCanvas.getGraphicsContext2D();

        // Set listeners
        UtilsViews.parentContainer.heightProperty().addListener((observable, oldValue, newvalue) -> {
            onSizeChanged();
        });
        UtilsViews.parentContainer.widthProperty().addListener((observable, oldValue, newvalue) -> {
            onSizeChanged();
        });

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

        newPosition.put("clientId", playerID);

        // Enviar la posición al servidor
        sendPositionToServer(newPosition);

        // Mensaje de depuración
        // System.out.println("Posición de mouse del usuario enviada: X=" + mouseX + ",
        // Y=" + mouseY);
    }

    private void onMousePressed(MouseEvent event) {

        double mouseX = event.getX();
        double mouseY = event.getY();

        /*
         * selectedObject = "";
         * 
         * for (String objectId : selectableObjects.keySet()) {
         * JSONObject obj = (JSONObject) selectableObjects.get(objectId);
         * int objX = obj.getInt("x");
         * int objY = obj.getInt("y");
         * int cols = obj.getInt("cols");
         * int rows = obj.getInt("rows");
         * 
         * if (isPositionInsideObject(mouseX, mouseY, objX, objY, cols, rows)) {
         * selectedObject = objectId;
         * //System.out.println("Barco " + selectedObject + " clickeado2");
         * break;
         * }
         * }
         */
    }

    private void onMouseDragged(MouseEvent event) {
        setOnMouseMoved(event);
    }

    private void onMouseReleased(MouseEvent event) {
        double mouseX = event.getX();
        double mouseY = event.getY();

        int col = attackGrid.getCol(mouseX);
        int row = attackGrid.getRow(mouseY);

        if (col != -1 && row != -1) {
            System.out.println("Atacando a la celda " + col + ", " + row);
            if (enemyPaintBoard[col][row] == null) {
                sendAttackMessage(col, row);
            }
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
        if (animationTimer.fps < 1) {
            return;
        }
        // Update objects and animations here
    }

    // Draw game to canvas
    public void draw() {
        // Limpiar el área de dibujo
        gcAttack.clearRect(0, 0, attackCanvas.getWidth(), attackCanvas.getHeight());
        gcDefense.clearRect(0, 0, defenseCanvas.getWidth(), defenseCanvas.getHeight());

        // Dibujar la celda resaltada en rosa
        if (highlightedCol >= 0 && highlightedRow >= 0) {
            double cellX = attackGrid.getCellX(highlightedCol);
            double cellY = attackGrid.getCellY(highlightedRow);
            double cellSize = attackGrid.getCellSize();
            double padding = 4;

            // Configura el color de la línea (puedes cambiarlo si deseas)
            gcAttack.setStroke(Color.RED); // Cambia el color de la "X" si lo deseas

            // Dibuja la "X"
            gcAttack.strokeLine(cellX + padding, cellY + padding, cellX + cellSize - padding,
                    cellY + cellSize - padding);
            gcAttack.strokeLine(cellX + cellSize - padding, cellY + padding, cellX + padding,
                    cellY + cellSize - padding);
        }

        // Dibujar las cuadrículas
        drawGrid();

        // Dibujar objetos seleccionables
        for (String objectId : selectableObjects.keySet()) {
            JSONObject selectableObject = (JSONObject) selectableObjects.get(objectId);
            drawSelectableObject(objectId, selectableObject);
        }

        doUserPaintBoard();
        doEnemyPaintBoard();

        // Dibujar el cursor en cada cuadrícula
        for (String clientId : clientMousePositions.keySet()) {
            JSONObject position = clientMousePositions.get(clientId);
            double mouseX = position.getDouble("x");
            double mouseY = position.getDouble("y");

            gcDefense.setFill(Color.RED);
            gcDefense.fillOval(mouseX - 5, mouseY - 5, 10, 10);
        }

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
        /*
         * System.out.println("Object ID: " + objectId);
         * System.out.println("Object Data: " + obj);
         */
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
            // System.out.println("ES HORIZONTAL");
            width = height;
            height = cellSize;
        } else {
            // System.out.println("ES VERTICAL");
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

        // System.out.println("(" + x + "," + y + ")" + width + " " + height);

        // Draw the rectangle
        gcDefense.setFill(color);
        gcDefense.fillRect(x, y, width, height);

        // Draw the outline
        gcDefense.setStroke(Color.BLACK);
        gcDefense.strokeRect(x, y, width, height);
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

    private void doUserPaintBoard() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                double cellSize = defenseGrid.getCellSize();
                double x = defenseGrid.getStartX() + i * cellSize;
                double y = defenseGrid.getStartY() + j * cellSize;
                if (userPaintBoard[i][j] == null) {
                    // continue;
                } else if (userPaintBoard[i][j]) {
                    gcDefense.setFill(Color.RED);
                    gcDefense.fillRect(x, y, cellSize, cellSize);
                } else if (!userPaintBoard[i][j]) {
                    gcDefense.setFill(Color.LIGHTBLUE);
                    gcDefense.fillRect(x, y, cellSize, cellSize);
                }
            }
        }
    }

    private void doEnemyPaintBoard() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                double cellSize = attackGrid.getCellSize();
                double x = attackGrid.getStartX() + i * cellSize;
                double y = attackGrid.getStartY() + j * cellSize;
                if (enemyPaintBoard[i][j] == null) {
                    //
                } else if (enemyPaintBoard[i][j]) {
                    gcAttack.setFill(Color.RED);
                    gcAttack.fillRect(x, y, cellSize, cellSize);
                    gcDefense.setStroke(Color.BLACK);
                    gcDefense.strokeRect(x, y, cellSize, cellSize);
                } else if (!enemyPaintBoard[i][j]) {
                    gcAttack.setFill(Color.LIGHTBLUE);
                    gcAttack.fillRect(x, y, cellSize, cellSize);
                    gcAttack.setStroke(Color.BLACK);
                    gcAttack.strokeRect(x, y, cellSize, cellSize);
                }
            }
        }
    }

    private void changeUserPaintBoard(boolean estado, int x, int y) {
        if (x >= 0 && x < 10 && y >= 0 && y < 10) {
            userPaintBoard[x][y] = estado;
            // System.out.println("Estado cambiado en la posición (" + x + ", " + y + ") a "
            // + estado);
        } else {
            System.out.println("Índices fuera de rango: (" + x + ", " + y + ")");
        }
    }

    private void changeEnemyPaintBoard(boolean estado, int x, int y) {
        if (x >= 0 && x < 10 && y >= 0 && y < 10) {
            enemyPaintBoard[x][y] = estado;
            // System.out.println("Estado cambiado en la posición (" + x + ", " + y + ") a "
            // + estado);
        } else {
            System.out.println("Índices fuera de rango: (" + x + ", " + y + ")");
        }
    }

    public void createOverlay() {
        overlayPane.setMouseTransparent(false); // Habilitar el pane superpuesto para capturar eventos
        overlayPane.setVisible(true); // Hacerlo visible

    }

    public void removeOverlay() {
        overlayPane.setVisible(false); // Ocultar el pane
    }

    private void sendAttackMessage(int col, int row) {
        JSONObject attackMessage = new JSONObject();
        attackMessage.put("type", "attackShip");
        attackMessage.put("enemyId", enemyID);
        attackMessage.put("col", col);
        attackMessage.put("row", row);

        if (ControllerConnect.clienteWebSocket != null) {
            ControllerConnect.clienteWebSocket.send(attackMessage.toString());
        }
    }

    public void paintEnemyGrid(int col, int row, boolean hit) {
        enemyPaintBoard[col][row] = hit;
    }

    public void paintPlayerGrid(int col, int row, boolean hit) {
        userPaintBoard[col][row] = hit;
    }

}
