package com.example.puestosypintado.controladores;

import com.example.puestosypintado.Database.Conexion;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PagoController {

    @FXML private TableView<?> tvPagos;
    @FXML private TableColumn<?, ?> colIdPago;
    @FXML private TableColumn<?, ?> colClientePago;
    @FXML private TableColumn<?, ?> colMontoPago;
    @FXML private TableColumn<?, ?> colFechaPago;

    @FXML private TextField txtIdPago;
    @FXML private ComboBox<?> cmbClientePago;
    @FXML private TextField txtMontoPago;
    @FXML private ComboBox<?> cmbMetodoPago;
    @FXML private TextField txtFechaPago;
    @FXML private TextField txtSaldoPago;

    Conexion conexion = new Conexion();

    @FXML
    public void initialize() {
        // Inicialización de la pantalla de pagos
    }

    @FXML
    public void irHome(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/puestosypintado/General/Home.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void fnBuscarPago(ActionEvent event) {
        System.out.println("Buscar pago...");
    }

    @FXML
    public void fnRegistrarPago(ActionEvent event) {
        System.out.println("Registrar pago...");
    }

    @FXML
    public void fnBorrarPago(ActionEvent event) {
        System.out.println("Borrar pago...");
    }

    @FXML
    public void fnLimpiarPago(ActionEvent event) {
        System.out.println("Limpiar pago...");
    }
}
