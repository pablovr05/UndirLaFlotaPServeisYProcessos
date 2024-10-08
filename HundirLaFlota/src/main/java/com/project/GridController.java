package com.project;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

public class GridController implements Initializable {

    @FXML
    private GridPane shipGrid;

    private Button[][] buttons = new Button[10][10];

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Add border to the grid
        // Create and fill the grid with buttons
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                // Add the button to the grid filling all the space
                Button button = new Button();
                button.setPrefSize(70, 70);
                //button.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #000000");
                buttons[i][j] = button;
                shipGrid.add(button, i, j);
                button.setOnAction(event -> {
                    buttonClicked(button);
                });
            }
        }


    }

    private void buttonClicked(Button button) {
        if (button.getStyle().equals("-fx-background-color: #000000"))
            button.setStyle("-fx-background-color: #FFFFFF");
        else {
            button.setStyle("-fx-background-color: #000000");
        }
    }
}
