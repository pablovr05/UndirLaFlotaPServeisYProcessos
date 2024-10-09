package com.project;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class GridController implements Initializable{

    @FXML
    Canvas gridCanvas;
    
    private GridMaker grid;
    private GraphicsContext gc;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        this.gc = gridCanvas.getGraphicsContext2D();
        
        UtilsViews.parentContainer.heightProperty().add(1000.0);
        UtilsViews.parentContainer.widthProperty().add(1000.0);

        grid = new GridMaker(0, 0, 78, 9, 9);
        drawGrid();
    }  

    public void drawGrid() {
    gc.setStroke(Color.BLACK);
        for (int row = 0; row < grid.getRows(); row++) {
            for (int col = 0; col < grid.getCols(); col++) {
                double cellSize = grid.getCellSize();
                double x = grid.getStartX() + col * cellSize;
                double y = grid.getStartY() + row * cellSize;
                gc.strokeRect(x, y, cellSize, cellSize);
            }
        }
    }
}
