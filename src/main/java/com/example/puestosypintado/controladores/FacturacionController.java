package com.example.puestosypintado.controladores;

import com.example.puestosypintado.modelo.Factura;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javax.swing.JOptionPane;
import java.sql.*;

import com.example.puestosypintado.Database.*;

public class FacturacionController {

    Conexion conexion = new Conexion();

    // --- TABLA Y COLUMNAS ---
    @FXML private TableView<Factura> tvFacturas;
    @FXML private TableColumn<Factura, String> colid;
    @FXML private TableColumn<Factura, String> colClienteFac;
    @FXML private TableColumn<Factura, String> colestado;

    // --- CAMPOS DE TEXTO Y COMBOBOX ---
    @FXML private TextField txtBuscarFac;
    @FXML private TextField txtnombre;
    @FXML private TextField txttotal;
    @FXML private TextField txtmonto;
    @FXML private TextField txtAbono;
    @FXML private ComboBox<String> cmbmetodo_pago;

    @FXML
    public void initialize() {
        // Inicializar métodos de pago
        cmbmetodo_pago.getItems().addAll("Efectivo", "Tarjeta de Crédito", "Transferencia", "Cheque");

        actualizarLista();
    }

    public void actualizarLista(){
        colid.setCellValueFactory(cellData -> cellData.getValue().numFacProperty());
        colClienteFac.setCellValueFactory(cellData -> cellData.getValue().clienteFacProperty());
        colestado.setCellValueFactory(cellData -> cellData.getValue().estadoFacProperty());

        tvFacturas.setItems(observableFacturas());
    }

    @FXML
    protected ObservableList<Factura> observableFacturas() {

        ObservableList<Factura> lista = FXCollections.observableArrayList();

        String sql = "SELECT * FROM Facturas";

        try (Connection connection = conexion.estabecerConexion();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {

                lista.add(new Factura(
                        rs.getString("facturas_id"),
                        rs.getString("cliente"),
                        rs.getString("estado")
                ));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al obtener facturas: " + e.toString());
        }

        return lista;
    }

    private void buscarDatosFactura(String sql) {

        try (Connection connection = conexion.estabecerConexion();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {

                txtnombre.setText(rs.getString("cliente"));
                txttotal.setText(rs.getString("total"));
                txtmonto.setText(rs.getString("balance"));

            } else {
                JOptionPane.showMessageDialog(null, "Factura no encontrada");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.toString());
        }
    }

    @FXML
    public void fnBuscarFactura(ActionEvent event) {

        String numFactura = txtBuscarFac.getText().trim();

        if (numFactura.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Ingrese un número de factura");
            return;
        }

        String sql = "SELECT cliente, total, balance FROM Facturas WHERE facturas_id = " + numFactura;

        buscarDatosFactura(sql);
    }

    @FXML
    public void fnAplicarPago(ActionEvent event) {

        if (txtBuscarFac.getText().isEmpty()) return;

        String sql = "UPDATE Facturas SET balance=?, estado=? WHERE facturas_id=?";

        try (Connection connection = conexion.estabecerConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            double balance = Double.parseDouble(txtmonto.getText().trim());
            double abono = Double.parseDouble(txtAbono.getText().trim());

            double nuevoBalance = balance - abono;

            String estado = (nuevoBalance <= 0) ? "Pagada" : "Pendiente";

            ps.setDouble(1, nuevoBalance);
            ps.setString(2, estado);
            ps.setString(3, txtBuscarFac.getText().trim());

            if (ps.executeUpdate() == 1) {
                JOptionPane.showMessageDialog(null, "Pago aplicado");
                fnBuscarFactura(null);
                actualizarLista();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.toString());
        }
    }

    @FXML
    public void fnAnularFactura(ActionEvent event) {

        int confirm = JOptionPane.showConfirmDialog(
                null,
                "¿Anular esta venta?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "UPDATE Facturas SET estado='Anulada' WHERE facturas_id='"
                + txtBuscarFac.getText().trim() + "'";
            ejecutarSQL(sql);
        }
    }

    public void ejecutarSQL(String sql) {

        try (Connection connection = conexion.estabecerConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            int r = ps.executeUpdate();

            if (r == 1) {
                JOptionPane.showMessageDialog(null, "Acción ejecutada correctamente");
                actualizarLista();
                fnLimpiarPagos(null);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.toString());
        }
    }

    @FXML
    public void fnLimpiarPagos(ActionEvent event) {
        txtBuscarFac.clear();
        txtnombre.clear();
        txttotal.clear();
        txtmonto.clear();
        txtAbono.clear();
        cmbmetodo_pago.setValue(null);
    }

}