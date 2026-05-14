package com.example.puestosypintado.controladores;

import com.example.puestosypintado.Database.Conexion;
import com.example.puestosypintado.modelo.DetalleSubservicio;
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
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import javax.swing.JOptionPane;
import java.sql.*;

/**
 * Controlador: Pantalla 8 — Entrega del Vehículo
 * UPDATE → [tbl.Servicio_Pintura] via sp_EntregarVehiculo
 *          estado: 'terminado' → 'entregado'
 * Prerequisitos verificados antes de habilitar el botón:
 *   · fn_SaldoPendienteServicio = 0
 *   · Sin incidencias abiertas en vw_IncidenciasAbiertas
 * READ → [tbl.Servicio_Pintura], [tbl.Cliente], [tbl.Vehiculo]
 * READ → [tbl.Detalle_Servicio_Pintura] + [tbl.Subservicios] (trabajos realizados)
 * READ → fn_CalcularCostoTotalServicio, fn_SaldoPendienteServicio
 * INSERT → [tbl.Log_Auditoria] (automático dentro del SP)
 */
public class EntregaVehiculoController {

    Conexion conexion = new Conexion();

    // ─── FXML — Banda de verificación ────────────────────────────
    @FXML private Pane  paneVerificacion;
    @FXML private Label lblVerificacion;

    // ─── FXML — Datos del servicio ────────────────────────────────
    @FXML private TextField txtIdOrden;
    @FXML private TextField txtCliente;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtPlaca;
    @FXML private TextField txtVehiculo;

    // ─── FXML — Tabla trabajos realizados ────────────────────────
    @FXML private TableView<DetalleSubservicio>           tvSubserviciosRealizados;
    @FXML private TableColumn<DetalleSubservicio, String> colSubNombre;
    @FXML private TableColumn<DetalleSubservicio, String> colSubTecnico;
    @FXML private TableColumn<DetalleSubservicio, String> colSubFecFin;
    @FXML private TableColumn<DetalleSubservicio, String> colSubEstado;

    // ─── FXML — Resumen financiero ────────────────────────────────
    @FXML private TextField txtCostoTotal;
    @FXML private TextField txtTotalPagado;
    @FXML private TextField txtSaldoPendiente;

    // ─── FXML — Empleado que entrega ─────────────────────────────
    @FXML private TextField txtEmpleadoEntrega;
    @FXML private TextField txtIdUsuario;

    // ─── FXML — Botón confirmar ───────────────────────────────────
    @FXML private Button btnConfirmarEntrega;

    // ─── Estado de precondiciones ─────────────────────────────────
    private boolean saldoOk        = false;
    private boolean sinIncidencias = false;

    // ─── INITIALIZE ──────────────────────────────────────────────
    public void initialize() {

        colSubNombre.setCellValueFactory (c -> c.getValue().nombreProperty());
        colSubTecnico.setCellValueFactory(c -> c.getValue().tecnicoProperty());
        colSubFecFin.setCellValueFactory (c -> c.getValue().fechaFinProperty());
        colSubEstado.setCellValueFactory (c -> c.getValue().estadoProperty());

        cargarDatosServicio();
        cargarSubserviciosRealizados();
        cargarEmpleadoEntrega();
        evaluarPrecondiciones();
    }

    // ─── DATOS DEL SERVICIO ───────────────────────────────────────
    private void cargarDatosServicio() {
        if (Sesion.idServicioPintura == 0) return;

        String sql =
                "SELECT s.id_servicioPintura, "
                        + "       c.nombre AS cliente, c.telefono, "
                        + "       v.placa, v.marca + ' ' + v.modelo AS vehiculo, "
                        + "       dbo.fn_CalcularCostoTotalServicio(s.id_servicioPintura) AS costo_total, "
                        + "       ISNULL(SUM(pc.total), 0) AS pagado, "
                        + "       dbo.fn_SaldoPendienteServicio(s.id_servicioPintura)     AS saldo "
                        + "FROM [tbl.Servicio_Pintura] s "
                        + "JOIN [tbl.Cliente]  c ON s.fk_cliente  = c.id_cliente "
                        + "JOIN [tbl.Vehiculo] v ON s.fk_vehiculo = v.id_vehiculo "
                        + "LEFT JOIN [tbl.Detalle_Pago_Cliente] pc "
                        + "       ON pc.fk_servicioPintura = s.id_servicioPintura "
                        + "      AND pc.estado = 'pagado' "
                        + "WHERE s.id_servicioPintura = ? "
                        + "GROUP BY s.id_servicioPintura, c.nombre, c.telefono, "
                        + "         v.placa, v.marca, v.modelo";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Sesion.idServicioPintura);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                txtIdOrden.setText  (rs.getString("id_servicioPintura"));
                txtCliente.setText  (rs.getString("cliente"));
                txtTelefono.setText (rs.getString("telefono") == null ? "" : rs.getString("telefono"));
                txtPlaca.setText    (rs.getString("placa"));
                txtVehiculo.setText (rs.getString("vehiculo"));

                double costoTotal = rs.getDouble("costo_total");
                double pagado     = rs.getDouble("pagado");
                double saldo      = rs.getDouble("saldo");

                txtCostoTotal.setText    (String.format("%.2f", costoTotal));
                txtTotalPagado.setText   (String.format("%.2f", pagado));
                txtSaldoPendiente.setText(String.format("%.2f", saldo));

                saldoOk = saldo <= 0;

                // Colorear saldo
                txtSaldoPendiente.setStyle(saldoOk
                        ? "-fx-text-fill: #00AA77; -fx-font-family: 'Courier New'; -fx-font-size: 13px; -fx-font-weight: bold;"
                        : "-fx-text-fill: #CC0000; -fx-font-family: 'Courier New'; -fx-font-size: 13px; -fx-font-weight: bold;");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error cargando servicio: " + e.getMessage());
        }
    }

    // ─── TRABAJOS REALIZADOS ──────────────────────────────────────
    private void cargarSubserviciosRealizados() {
        ObservableList<DetalleSubservicio> lista = FXCollections.observableArrayList();

        String sql =
                "SELECT d.id_detalleServicio, sb.nombre, sb.precio_base, d.estado, "
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
            }
            tvSubserviciosRealizados.setItems(lista);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error cargando trabajos: " + e.getMessage());
        }
    }

    // ─── EMPLEADO QUE ENTREGA ─────────────────────────────────────
    private void cargarEmpleadoEntrega() {
        if (Sesion.idUsuario == 0) return;

        String sql =
                "SELECT e.nombre_completo "
                        + "FROM [tbl.Empleado] e "
                        + "JOIN [tbl.Usuario]  u ON e.id_empleado = u.fk_empleado "
                        + "WHERE u.id_usuario = ?";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Sesion.idUsuario);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtEmpleadoEntrega.setText(rs.getString("nombre_completo"));
                txtIdUsuario.setText      (String.valueOf(Sesion.idUsuario));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error cargando empleado: " + e.getMessage());
        }
    }

    // ─── VERIFICAR PRECONDICIONES ─────────────────────────────────
    // Saldo = 0  Y  sin incidencias abiertas
    private void evaluarPrecondiciones() {

        // Verificar incidencias abiertas
        String sql =
                "SELECT COUNT(*) AS total "
                        + "FROM [tbl.Incidencias_Servicio] "
                        + "WHERE fk_servicioPintura = ? "
                        + "  AND estado IN ('abierta','en_correccion')";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Sesion.idServicioPintura);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                sinIncidencias = rs.getInt("total") == 0;
            }

        } catch (Exception e) {
            sinIncidencias = false;
        }

        boolean listo = saldoOk && sinIncidencias;
        btnConfirmarEntrega.setDisable(!listo);

        if (listo) {
            paneVerificacion.setStyle("-fx-background-color: #00AA77;");
            lblVerificacion.setText(
                    "✓  LISTO PARA ENTREGA — Sin saldo pendiente ni incidencias abiertas");
        } else {
            paneVerificacion.setStyle("-fx-background-color: #CC3300;");

            StringBuilder motivos = new StringBuilder("✗  NO SE PUEDE ENTREGAR —");
            if (!saldoOk)        motivos.append("  Saldo pendiente sin pagar.");
            if (!sinIncidencias) motivos.append("  Existen incidencias abiertas.");
            lblVerificacion.setText(motivos.toString());
        }
    }

    // ─── CONFIRMAR ENTREGA ────────────────────────────────────────
    @FXML
    public void fnConfirmarEntrega(ActionEvent event) {

        if (!saldoOk || !sinIncidencias) {
            JOptionPane.showMessageDialog(null,
                    "No se cumplen las condiciones para entregar el vehículo.");
            return;
        }
        if (txtIdUsuario.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay usuario de sesión activo.");
            return;
        }

        // Confirmar con el empleado
        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Confirma la entrega del vehículo al cliente?\n"
                        + "Esta acción no se puede deshacer.",
                ButtonType.YES, ButtonType.NO);
        confirmacion.setTitle("Confirmar entrega");
        if (confirmacion.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;

        // Llamar sp_EntregarVehiculo
        String sql = "{call sp_EntregarVehiculo(?, ?)}";

        try (Connection conn = conexion.estabecerConexion();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt(1, Sesion.idServicioPintura);
            cs.setInt(2, Sesion.idUsuario);
            cs.execute();

            JOptionPane.showMessageDialog(null,
                    "Vehículo entregado correctamente.\nOrden #"
                            + Sesion.idServicioPintura + " cerrada.");

            Sesion.limpiarNavegacion();
            fnCancelar(event);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al entregar: " + e.getMessage());
        }
    }

    // ─── CANCELAR / VOLVER ────────────────────────────────────────
    @FXML
    public void fnCancelar(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(
                    "/com/example/puestosypintado/pintura/DetalleServicio.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}