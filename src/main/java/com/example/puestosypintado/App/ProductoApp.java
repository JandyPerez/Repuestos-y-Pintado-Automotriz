package com.example.puestosypintado.App;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ProductoApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ProductoApp.class.getResource("/com/example/puestosypintado/Venta/Producto.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 750, 480);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }
}
