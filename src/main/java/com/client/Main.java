package com.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    public static Stage stageFX;

    @Override
    public void start(Stage stage) throws Exception {

        stageFX = stage;
        // Carrega la vista inicial des del fitxer FXML
        Parent root = FXMLLoader.load(getClass().getResource("/assets/layout_connect.fxml"));
        Scene scene = new Scene(root);

        stageFX.setScene(scene);
        stageFX.setResizable(true);
        stageFX.setTitle("JavaFX App");
        stageFX.getIcons().add(new Image("/images/UndirLaFlotaLogo.png"));
        stageFX.show();

        // Afegeix una icona només si no és un Mac
        if (!System.getProperty("os.name").contains("Mac")) {
            stageFX.getIcons().add(new Image("/images/UndirLaFlotaLogo.png"));
        }
    }

    @Override
    public void stop() {
        System.exit(1); // Kill all executor services
    }

    public static Stage getStage() {
        return stageFX;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
