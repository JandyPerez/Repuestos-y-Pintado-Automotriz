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

public class VentaController {

    @FXML private TableView<?> tvVentas;
    @FXML private TableColumn<?, ?> colIdVenta;
    @FXML private TableColumn<?, ?> colClienteVenta;
    @FXML private TableColumn<?, ?> colTotalVenta;
    @FXML private TableColumn<?, ?> colEstadoVenta;

    @FXML private TextField txtIdVenta;
    @FXML private ComboBox<?> cmbClienteVenta;
    @FXML private ComboBox<?> cmbVehiculoVenta;
    @FXML private ComboBox<?> cmbRepuestoVenta;
    @FXML private TextField txtCantVenta;

    @FXML private TableView<?> tvItemsVenta;
    @FXML private TableColumn<?, ?> colItemRepuesto;
    @FXML private TableColumn<?, ?> colItemCantidad;
    @FXML private TableColumn<?, ?> colItemPrecio;
    @FXML private TableColumn<?, ?> colItemSubtotal;

    @FXML private TextField txtSubtotalVenta;
    @FXML private TextField txtImpuestosVenta;
    @FXML private TextField txtTotalVenta;
    @FXML private ComboBox<?> cmbMetodoPagoVenta;

    Conexion conexion = new Conexion();

    @FXML
    public void initialize() {
        // Inicialización de la pantalla
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
    public void fnBuscarVenta(ActionEvent event) {
        System.out.println("Buscar venta...");
    }

    @FXML
    public void fnAgregarItem(ActionEvent event) {
        System.out.println("Añadir item...");
    }

    @FXML
    public void fnConfirmarVenta(ActionEvent event) {
        System.out.println("Confirmar venta...");
    }

    @FXML
    public void fnQuitarItem(ActionEvent event) {
        System.out.println("Quitar item...");
    }

    @FXML
    public void fnLimpiarVenta(ActionEvent event) {
        System.out.println("Limpiar venta...");
    }
}
