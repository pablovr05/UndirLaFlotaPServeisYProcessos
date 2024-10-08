package com.project;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {


    @Override
    public void start(Stage stage) throws Exception {

        // Carrega la vista inicial des del fitxer FXML
        Parent root = FXMLLoader.load(getClass().getResource("/assets/layout_connect.fxml"));
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Undir la flota");
        Image icon = new Image("file:icons/UndirLaFlotaLogo.png");
        stage.getIcons().add(icon);
        stage.show();

        // Afegeix una icona només si no és un Mac
        if (!System.getProperty("os.name").contains("Mac")) {
            icon = new Image("file:icons/UndirLaFlotaLogo.png");
            stage.getIcons().add(icon);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
