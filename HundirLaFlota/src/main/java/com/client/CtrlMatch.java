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

public class CtrlMatch implements Initializable {


    @FXML
    private Canvas attackCanvas;
    @FXML
    private Canvas defenseCanvas;
    private GraphicsContext gcAttack, gcDefense;

    private String enemyID;
    private String playerID;


    private PlayTimer animationTimer;
    private PlayGrid attackGrid, defenseGrid;

    public Map<String, JSONObject> clientMousePositions = new HashMap<>();

    public  Map<String, JSONObject> selectableObjects = new HashMap<>();
    private String selectedObject = "";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Change window size
        this.selectableObjects = ControllerPlay.instance.getSelectableObjects();

        onSizeChanged();
        attackCanvas.setVisible(true);
        defenseCanvas.setVisible(true);

        // Get drawing context
        gcAttack = attackCanvas.getGraphicsContext2D();
        gcDefense = defenseCanvas.getGraphicsContext2D();

        // Set listeners
        UtilsViews.parentContainer.heightProperty().addListener((observable, oldValue, newvalue) -> { onSizeChanged(); });
        UtilsViews.parentContainer.widthProperty().addListener((observable, oldValue, newvalue) -> { onSizeChanged(); });

        attackCanvas.setOnMouseMoved(this::setOnMouseMoved);
        attackCanvas.setOnMousePressed(this::onMousePressed);
        attackCanvas.setOnMouseDragged(this::onMouseDragged);
        attackCanvas.setOnMouseReleased(this::onMouseReleased);

        // Define grids
        defenseGrid = new PlayGrid(50, 50, 40, 10, 10);
        attackGrid = new PlayGrid(50, 50, 40, 10, 10);

        // Start run/draw timer bucle
        animationTimer = new PlayTimer(this::run, this::draw, 0);
        start();
    }

    // When window changes its size
    public void onSizeChanged() {
        double width = UtilsViews.parentContainer.getWidth();
        double height = UtilsViews.parentContainer.getHeight();
        defenseCanvas.setWidth(width);
        defenseCanvas.setHeight(height);
        attackCanvas.setWidth(width);
        attackCanvas.setHeight(height);
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
            newPosition.put("col", attackGrid.getCol(mouseX));
            newPosition.put("row", attackGrid.getRow(mouseY));
        } else {
            newPosition.put("col", -1);
            newPosition.put("row", -1);
        }
        /*clientMousePositions.put(Main.clientId, newPosition);

        JSONObject msgObj = clientMousePositions.get(Main.clientId);
        msgObj.put("type", "clientMouseMoving");
        msgObj.put("clientId", Main.clientId);
*/
        /*if (Main.wsClient != null) {
            Main.wsClient.safeSend(msgObj.toString());
        }*/
    }

    private void onMousePressed(MouseEvent event) {

        double mouseX = event.getX();
        double mouseY = event.getY();

        selectedObject = "";

        for (String objectId : selectableObjects.keySet()) {
            JSONObject obj = selectableObjects.get(objectId);
            int objX = obj.getInt("x");
            int objY = obj.getInt("y");
            int cols = obj.getInt("cols");
            int rows = obj.getInt("rows");

            /*if (isPositionInsideObject(mouseX, mouseY, objX, objY,  cols, rows)) {
                selectedObject = objectId;
                System.out.println("Barco " + selectedObject + " clickeado2");
                break;
            }*/
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
        System.out.println("Drawing frame"); // Debug print

        // Clean drawing area
        gcAttack.clearRect(0, 0, attackCanvas.getWidth(), attackCanvas.getHeight());
        gcDefense.clearRect(0, 0, defenseCanvas.getWidth(), defenseCanvas.getHeight());

        // Draw colored 'over' cells

        for (String clientId : clientMousePositions.keySet()) {
            JSONObject position = clientMousePositions.get(clientId);

            int col = position.getInt("col");
            int row = position.getInt("row");

            // Comprovar si està dins dels límits de la graella
            if (row >= 0 && col >= 0) {
                if ("A".equals(clientId)) {
                    gcAttack.setFill(Color.LIGHTBLUE);
                } else {
                    gcAttack.setFill(Color.LIGHTGREEN);
                }
                // Emplenar la casella amb el color clar
                gcAttack.fillRect(attackGrid.getCellX(col), attackGrid.getCellY(row), attackGrid.getCellSize(), attackGrid.getCellSize());
            }
        }

        // Draw grids
        drawGrid();

        System.out.println("attackCanvas width: " + attackCanvas.getWidth() + ", height: " + attackCanvas.getHeight());
        System.out.println("defenseCanvas width: " + defenseCanvas.getWidth() + ", height: " + defenseCanvas.getHeight());

        // Draw selectable objects
        for (String objectId : selectableObjects.keySet()) {
            JSONObject selectableObject = selectableObjects.get(objectId);
            drawSelectableObject(objectId, selectableObject);
        }

        // Draw mouse circles
        for (String clientId : clientMousePositions.keySet()) {
            JSONObject position = clientMousePositions.get(clientId);
            if ("A".equals(clientId)) {
                gcAttack.setFill(Color.BLUE);
            } else {
                gcDefense.setFill(Color.GREEN);
            }
            gcAttack.fillOval(position.getInt("x") - 5, position.getInt("y") - 5, 10, 10);
        }

        // Draw FPS if needed
        //if (showFPS) { animationTimer.drawFPS(gcAttack); }
    }

    public void updateGrid(int col, int row, Color color, PlayGrid grid, GraphicsContext gc) {
        double cellSize = grid.getCellSize();
        double x = grid.getStartX() + col * cellSize;
        double y = grid.getStartY() + row * cellSize;

        gc.setFill(color);
        gc.fillRect(x, y, cellSize, cellSize);
    }

    public void drawGrid() {
        System.out.println("Drawing grid 1"); // Debug print
        gcDefense.setStroke(Color.DARKGREEN);
        drawGrids(defenseGrid, gcDefense);
        System.out.println("Drawing grid 2"); // Debug print
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
        gcDefense.save(); // Guarda el estado actual del contexto gráfico
        gcDefense.translate(x + width / 2, y + height / 2); // Mueve el origen al centro del objeto
        gcDefense.rotate(rotation); // Aplica la rotación si es necesaria
        gcDefense.translate(-width / 2, -height / 2); // Mueve de nuevo el origen al ángulo original

        // Seleccionar un color basado en el objectId
        Color color = switch (objectId.toLowerCase()) {
            case "red" -> Color.RED;
            case "blue" -> Color.BLUE;
            case "green" -> Color.GREEN;
            case "yellow" -> Color.YELLOW;
            default -> Color.GRAY;
        };

        // Dibujar el rectángulo
        gcDefense.setFill(color);
        gcDefense.fillRect(0, 0, width, height);

        // Dibujar el contorno
        gcDefense.setStroke(Color.BLACK);
        gcDefense.strokeRect(0, 0, width, height);

        // Opcionalmente, agregar texto (por ejemplo, el objectId)
        gcDefense.setFill(Color.BLACK);
        gcDefense.fillText(objectId, 5, 15);

        gcDefense.restore(); // Restaura el contexto gráfico
    }

    private void sendAttackMessage(double mouseX, double mouseY) {
        JSONObject attackMessage = new JSONObject();
        double cellSize = attackGrid.getCellSize();

        attackMessage.put("type", "attack");
        attackMessage.put("mouseX", mouseX);
        attackMessage.put("mouseY", mouseY);
        attackMessage.put("cellsize", cellSize);

        /*if (Main.wsClient != null) {
            Main.wsClient.safeSend(attackMessage.toString());
        }*/
    }
}
