module com.example.puestosypintado {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    opens com.example.puestosypintado.controladores to javafx.fxml;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires java.desktop;
    requires javafx.graphics;
    requires javafx.base;

    opens com.example.puestosypintado to javafx.fxml;
    exports com.example.puestosypintado;
    exports com.example.puestosypintado.App;
    opens com.example.puestosypintado.App to javafx.fxml;
}