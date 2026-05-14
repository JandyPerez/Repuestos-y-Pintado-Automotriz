package com.example.puestosypintado.controladores;

import com.example.puestosypintado.Database.Conexion;
import com.example.puestosypintado.modelo.DetalleSubservicio;
import com.example.puestosypintado.modelo.Subservicio;
import com.example.puestosypintado.util.Sesion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import javax.swing.JOptionPane;
import java.sql.*;

/**
 * Controlador: Pantalla 3 — Agregar Subservicios a la Orden
 * SELECT → [tbl.Subservicios]
 * INSERT → [tbl.Detalle_Servicio_Pintura] via sp_AgregarSubservicio
 * DELETE → [tbl.Detalle_Servicio_Pintura] (quitar subservicio)
 */
public class SubserviciosController {

    Conexion conexion = new Conexion();

    // ─── FXML — Barra de contexto ─────────────────────────────────
    @FXML private TextField txtIdOrden;
    @FXML private TextField txtClienteOrden;
    @FXML private TextField txtVehiculoOrden;
    @FXML private TextField txtCostoTotal;

    // ─── FXML — Panel izquierdo (catálogo) ───────────────────────
    @FXML private TableView<Subservicio>            tvCatalogo;
    @FXML private TableColumn<Subservicio, String>  colCatNombre;
    @FXML private TableColumn<Subservicio, String>  colCatPrecioBase;
    @FXML private TextField                         txtDescSubservicio;

    // ─── FXML — Panel derecho (subservicios de la orden) ─────────
    @FXML private TableView<DetalleSubservicio>             tvOrdenSubservicios;
    @FXML private TableColumn<DetalleSubservicio, String>   colDetalleId;
    @FXML private TableColumn<DetalleSubservicio, String>   colDetalleNombre;
    @FXML private TableColumn<DetalleSubservicio, String>   colDetallePrecio;
    @FXML private TableColumn<DetalleSubservicio, String>   colDetalleEstado;
    @FXML private TextField                                  txtSumaPreciosBase;

    // ─── INITIALIZE ──────────────────────────────────────────────
    public void initialize() {

        // Catálogo
        colCatNombre.setCellValueFactory    (c -> c.getValue().nombreProperty());
        colCatPrecioBase.setCellValueFactory(c -> c.getValue().precioBaseProperty());

        // Orden
        colDetalleId.setCellValueFactory    (c -> c.getValue().idDetalleProperty());
        colDetalleNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colDetallePrecio.setCellValueFactory(c -> c.getValue().precioBaseProperty());
        colDetalleEstado.setCellValueFactory(c -> c.getValue().estadoProperty());

        // Mostrar descripción al seleccionar del catálogo
        tvCatalogo.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null)
                        txtDescSubservicio.setText(newVal.getDescripcion());
                });

        cargarContextoOrden();
        cargarCatalogo();
        cargarSubserviciosOrden();
    }

    // ─── CONTEXTO DE LA ORDEN ────────────────────────────────────
    private void cargarContextoOrden() {
        if (Sesion.idServicioPintura == 0) return;

        String sql = "SELECT s.id_servicioPintura, c.nombre AS cliente, "
                + "       v.placa + ' - ' + v.marca + ' ' + v.modelo AS vehiculo "
                + "FROM [tbl.Servicio_Pintura] s "
                + "JOIN [tbl.Cliente]  c ON s.fk_cliente  = c.id_cliente "
                + "JOIN [tbl.Vehiculo] v ON s.fk_vehiculo = v.id_vehiculo "
                + "WHERE s.id_servicioPintura = ?";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Sesion.idServicioPintura);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtIdOrden.setText     (rs.getString("id_servicioPintura"));
                txtClienteOrden.setText(rs.getString("cliente"));
                txtVehiculoOrden.setText(rs.getString("vehiculo"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error cargando contexto: " + e.getMessage());
        }
        actualizarCostoTotal();
    }

    // ─── CATÁLOGO DE SUBSERVICIOS ─────────────────────────────────
    private void cargarCatalogo() {
        ObservableList<Subservicio> lista = FXCollections.observableArrayList();

        String sql = "SELECT id_subservicio, nombre, descripcion, precio_base, estado "
                + "FROM [tbl.Subservicios] WHERE estado = 'ACTIVO' ORDER BY nombre";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Subservicio(
                        rs.getString("id_subservicio"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        String.format("%.2f", rs.getDouble("precio_base")),
                        rs.getString("estado")
                ));
            }
            tvCatalogo.setItems(lista);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error cargando catálogo: " + e.getMessage());
        }
    }

    // ─── SUBSERVICIOS YA AÑADIDOS A ESTA ORDEN ───────────────────
    private void cargarSubserviciosOrden() {
        ObservableList<DetalleSubservicio> lista = FXCollections.observableArrayList();

        String sql = "SELECT d.id_detalleServicio, sb.nombre, sb.precio_base, "
                + "       d.estado, "
                + "       ISNULL(e.nombre_completo, '--') AS tecnico, "
                + "       ISNULL(CONVERT(VARCHAR, d.fecha_fin, 103), '--') AS fecha_fin, "
                + "       ISNULL(d.observaciones, '') AS obs "
                + "FROM [tbl.Detalle_Servicio_Pintura] d "
                + "JOIN [tbl.Subservicios] sb ON d.fk_subservicio = sb.id_subservicio "
                + "LEFT JOIN [tbl.Empleado] e ON d.fk_empleado    = e.id_empleado "
                + "WHERE d.fk_servicioPintura = ? "
                + "ORDER BY d.id_detalleServicio";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Sesion.idServicioPintura);
            ResultSet rs = ps.executeQuery();

            double suma = 0.0;
            while (rs.next()) {
                lista.add(new DetalleSubservicio(
                        rs.getString("id_detalleServicio"),
                        rs.getString("nombre"),
                        String.format("%.2f", rs.getDouble("precio_base")),
                        rs.getString("estado"),
                        rs.getString("tecnico"),
                        rs.getString("fecha_fin"),
                        rs.getString("obs")
                ));
                suma += rs.getDouble("precio_base");
            }
            tvOrdenSubservicios.setItems(lista);
            txtSumaPreciosBase.setText(String.format("%.2f", suma));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error cargando subservicios: " + e.getMessage());
        }
    }

    // ─── AGREGAR SUBSERVICIO ──────────────────────────────────────
    @FXML
    public void fnAgregarSubservicio(ActionEvent event) {

        Subservicio seleccionado = tvCatalogo.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            JOptionPane.showMessageDialog(null,
                    "Seleccione un subservicio del catálogo.");
            return;
        }

        // Llamar sp_AgregarSubservicio
        String sql = "{call sp_AgregarSubservicio(?, ?)}";

        try (Connection conn = conexion.estabecerConexion();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt(1, Sesion.idServicioPintura);
            cs.setInt(2, Integer.parseInt(seleccionado.getIdSubservicio()));
            cs.execute();

            cargarSubserviciosOrden();
            actualizarCostoTotal();

        } catch (Exception e) {
            // El SP lanza error si ya existe; mostrar mensaje limpio
            String msg = e.getMessage();
            if (msg != null && msg.contains("ya está vinculado")) {
                JOptionPane.showMessageDialog(null,
                        "Ese subservicio ya fue añadido a esta orden.");
            } else {
                JOptionPane.showMessageDialog(null, "Error al agregar: " + msg);
            }
        }
    }

    // ─── QUITAR SUBSERVICIO ───────────────────────────────────────
    @FXML
    public void fnQuitarSubservicio(ActionEvent event) {

        DetalleSubservicio seleccionado =
                tvOrdenSubservicios.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            JOptionPane.showMessageDialog(null,
                    "Seleccione un subservicio de la orden para quitarlo.");
            return;
        }

        // Solo se puede quitar si aún está pendiente
        if (!"pendiente".equals(seleccionado.getEstado())) {
            JOptionPane.showMessageDialog(null,
                    "Solo se pueden quitar subservicios en estado 'pendiente'.");
            return;
        }

        String sql = "DELETE FROM [tbl.Detalle_Servicio_Pintura] "
                + "WHERE id_detalleServicio = ?";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(seleccionado.getIdDetalle()));
            if (ps.executeUpdate() == 1) {
                cargarSubserviciosOrden();
                actualizarCostoTotal();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al quitar: " + e.getMessage());
        }
    }

    // ─── COSTO TOTAL (fn_CalcularCostoTotalServicio) ──────────────
    private void actualizarCostoTotal() {
        String sql = "SELECT dbo.fn_CalcularCostoTotalServicio(?) AS total";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Sesion.idServicioPintura);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                txtCostoTotal.setText(String.format("%.2f", rs.getDouble("total")));

        } catch (Exception e) {
            txtCostoTotal.setText("—");
        }
    }

    // ─── NAVEGACIÓN ──────────────────────────────────────────────
    @FXML
    public void fnVolverOrden(ActionEvent event) {
        navegarA("/com/example/puestosypintado/pintura/NuevaOrdenServicio.fxml", event);
    }

    @FXML
    public void fnIrDetalle(ActionEvent event) {
        if (tvOrdenSubservicios.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Agregue al menos un subservicio antes de ver el detalle.");
            return;
        }
        navegarA("/com/example/puestosypintado/pintura/DetalleServicio.fxml", event);
    }

    @FXML
    public void irHome(ActionEvent event) {
        navegarA("/com/example/puestosypintado/General/Home.fxml", event);
    }

    private void navegarA(String fxml, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}