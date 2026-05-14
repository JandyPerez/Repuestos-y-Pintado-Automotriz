package com.example.puestosypintado.controladores;

import com.example.puestosypintado.Database.Conexion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class VentasController {

    @FXML private TableView<Object> tvCarrito;
    @FXML private TableColumn<Object, String> colProducto;
    @FXML private TableColumn<Object, Integer> colCant;
    @FXML private TableColumn<Object, Double> colSubtotal;

    @FXML private TextField txtBuscadorCliente;
    @FXML private TextField txtBuscadorProd;
    @FXML private TextField txtCantidad;

    @FXML private Label lblImpuestos;
    @FXML private Label lblTotalCarrito;
    @FXML private Label lblStock;

    Conexion conexion = new Conexion();

    private ObservableList<Object> listaCarrito = FXCollections.observableArrayList();

    private String clienteSeleccionado = null;

    private double totalGeneral = 0;

    // =====================================================
    public void initialize() {
        tvCarrito.setItems(listaCarrito);

        lblStock.setText("0");
        lblImpuestos.setText("$ 0.00");
        lblTotalCarrito.setText("$ 0.00");
    }

    // =====================================================
    // BUSCAR CLIENTE (NUEVO)
    // =====================================================
    @FXML
    public void fnBuscar(ActionEvent event) {

        String cliente = txtBuscadorCliente.getText().trim();

        if (cliente.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Ingrese nombre o cédula del cliente.");
            return;
        }

        String sql = "SELECT nombre, cedula FROM Clientes WHERE nombre = ? OR cedula = ?";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cliente);
            ps.setString(2, cliente);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                clienteSeleccionado = rs.getString("nombre");

                JOptionPane.showMessageDialog(null,
                        "Cliente encontrado:\n" + clienteSeleccionado);

            } else {
                clienteSeleccionado = null;
                JOptionPane.showMessageDialog(null, "Cliente no encontrado.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    // =====================================================
    // STOCK
    // =====================================================
    @FXML
    public void fnVerificarStock(ActionEvent event) {

        String producto = txtBuscadorProd.getText().trim();

        String sql = "SELECT stock_actual, stock_minimo FROM Repuestos WHERE sku = ? OR descripcion = ?";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, producto);
            ps.setString(2, producto);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                lblStock.setText(rs.getInt("stock_actual") + " / " + rs.getInt("stock_minimo"));
            } else {
                lblStock.setText("0 / 0");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    // =====================================================
    // AGREGAR AL CARRITO
    // =====================================================
    @FXML
    public void fnAgregarAlCarrito(ActionEvent event) {

        String producto = txtBuscadorProd.getText().trim();
        String cantidadTxt = txtCantidad.getText().trim();

        if (producto.isEmpty() || cantidadTxt.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Complete campos.");
            return;
        }

        try {
            int cantidad = Integer.parseInt(cantidadTxt);

            String sql = "SELECT precio_venta, stock_actual FROM Repuestos WHERE sku = ? OR descripcion = ?";

            try (Connection conn = conexion.estabecerConexion();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, producto);
                ps.setString(2, producto);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {

                    double precioVenta = rs.getDouble("precio_venta");
                    int stock = rs.getInt("stock_actual");

                    if (cantidad > stock) {
                        JOptionPane.showMessageDialog(null, "No hay stock suficiente.");
                        return;
                    }

                    double subtotal = precioVenta * cantidad;

                    totalGeneral += subtotal;

                    listaCarrito.add(new Object());

                    actualizarTotales();

                } else {
                    JOptionPane.showMessageDialog(null, "Producto no encontrado.");
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    // =====================================================
    // PROCESAR VENTA
    // =====================================================
    @FXML
    public void fnProcesarPago(ActionEvent event) {

        if (clienteSeleccionado == null) {
            JOptionPane.showMessageDialog(null, "Seleccione un cliente válido.");
            return;
        }

        if (listaCarrito.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Carrito vacío.");
            return;
        }

        JOptionPane.showMessageDialog(null,
                "Venta procesada para: " + clienteSeleccionado);

        fnCancelarVenta(null);
    }

    // =====================================================
    // LIMPIAR
    // =====================================================
    @FXML
    public void fnCancelarVenta(ActionEvent event) {

        txtBuscadorCliente.clear();
        txtBuscadorProd.clear();
        txtCantidad.clear();

        lblStock.setText("0");

        listaCarrito.clear();
        totalGeneral = 0;

        clienteSeleccionado = null;

        actualizarTotales();
    }

    private void actualizarTotales() {
        double impuestos = totalGeneral * 0.18;
        lblImpuestos.setText("$ " + String.format("%.2f", impuestos));
        lblTotalCarrito.setText("$ " + String.format("%.2f", totalGeneral + impuestos));
    }
}