package com.example.puestosypintado.App;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class UsuarioApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(UsuarioApp.class.getResource("/com/example/puestosypintado/General/Usuario.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 850, 435);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }
}
