package com.project;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class GridController implements Initializable{

    @FXML
    Canvas gridCanvas;
    
    private GridMaker grid;
    private GraphicsContext gc;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        this.gc = gridCanvas.getGraphicsContext2D();
        
        UtilsViews.parentContainer.heightProperty();
        grid = new GridMaker(200, 200, 40, 9, 9);
    }  


}
