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
import javafx.stage.Stage;

import javax.swing.JOptionPane;
import java.sql.*;

/**
 * Controlador: Pantalla 6 — Registrar Incidencia de Calidad
 * INSERT → [tbl.Incidencias_Servicio] via sp_RegistrarIncidencia
 * READ   → [tbl.Detalle_Servicio_Pintura] + [tbl.Subservicios] (ComboBox subservicio)
 * READ   → [tbl.Servicio_Pintura], [tbl.Cliente], [tbl.Vehiculo] (contexto)
 * READ   → [tbl.Empleado] via Sesion.idUsuario (supervisor)
 * Efecto → estado del servicio pasa a 'en_correccion'
 */
public class RegistrarIncidenciaController {

    Conexion conexion = new Conexion();

    // ─── FXML — Contexto (solo lectura) ──────────────────────────
    @FXML private TextField txtIdServicio;
    @FXML private TextField txtClienteCtx;
    @FXML private TextField txtPlacaCtx;

    // ─── FXML — Formulario ───────────────────────────────────────
    @FXML private ComboBox<String>           cmbTipoDefecto;
    @FXML private ComboBox<DetalleSubservicio> cmbSubservicioAfectado;
    @FXML private TextArea                   txtDescripcion;
    @FXML private TextField                  txtSupervisor;
    @FXML private TextField                  txtIdSupervisor;

    // ─── INITIALIZE ──────────────────────────────────────────────
    public void initialize() {

        // Valores del CHECK constraint en tbl.Incidencias_Servicio
        cmbTipoDefecto.setItems(FXCollections.observableArrayList(
                "color_incorrecto",
                "dano_carroceria",
                "masillado_defectuoso",
                "pintura_defectuosa",
                "superficie_no_debida",
                "barniz_defectuoso",
                "otro"
        ));

        cargarContexto();
        cargarSubserviciosOrden();
        cargarSupervisor();
    }

    // ─── CONTEXTO DEL SERVICIO ────────────────────────────────────
    private void cargarContexto() {
        if (Sesion.idServicioPintura == 0) return;

        String sql =
                "SELECT s.id_servicioPintura, c.nombre, v.placa "
                        + "FROM [tbl.Servicio_Pintura] s "
                        + "JOIN [tbl.Cliente]  c ON s.fk_cliente  = c.id_cliente "
                        + "JOIN [tbl.Vehiculo] v ON s.fk_vehiculo = v.id_vehiculo "
                        + "WHERE s.id_servicioPintura = ?";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Sesion.idServicioPintura);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtIdServicio.setText (rs.getString("id_servicioPintura"));
                txtClienteCtx.setText (rs.getString("nombre"));
                txtPlacaCtx.setText   (rs.getString("placa"));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error cargando contexto: " + e.getMessage());
        }
    }

    // ─── SUBSERVICIOS DE LA ORDEN (ComboBox) ──────────────────────
    private void cargarSubserviciosOrden() {
        ObservableList<DetalleSubservicio> lista = FXCollections.observableArrayList();

        // Añadir opción vacía (subservicio afectado es opcional)
        lista.add(new DetalleSubservicio("0", "-- Sin subservicio específico --",
                "0", "", "", "", ""));

        String sql =
                "SELECT d.id_detalleServicio, sb.nombre "
                        + "FROM [tbl.Detalle_Servicio_Pintura] d "
                        + "JOIN [tbl.Subservicios] sb ON d.fk_subservicio = sb.id_subservicio "
                        + "WHERE d.fk_servicioPintura = ? "
                        + "ORDER BY sb.nombre";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Sesion.idServicioPintura);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(new DetalleSubservicio(
                        rs.getString("id_detalleServicio"),
                        rs.getString("nombre"),
                        "0", "", "", "", ""
                ));
            }
            cmbSubservicioAfectado.setItems(lista);
            cmbSubservicioAfectado.setValue(lista.get(0));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error cargando subservicios: " + e.getMessage());
        }
    }

    // ─── SUPERVISOR (sesión activa) ───────────────────────────────
    private void cargarSupervisor() {
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
                txtSupervisor.setText(rs.getString("nombre_completo"));
                txtIdSupervisor.setText(String.valueOf(Sesion.idUsuario));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error cargando supervisor: " + e.getMessage());
        }
    }

    // ─── GUARDAR INCIDENCIA ───────────────────────────────────────
    @FXML
    public void fnGuardarIncidencia(ActionEvent event) {

        // Validaciones
        if (cmbTipoDefecto.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Seleccione el tipo de defecto.");
            return;
        }
        if (txtDescripcion.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "La descripción es obligatoria.");
            return;
        }
        if (txtIdSupervisor.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay supervisor de sesión activo.");
            return;
        }

        // Obtener id del empleado supervisor desde el usuario de sesión
        int idSupervisor = obtenerIdEmpleado(Sesion.idUsuario);
        if (idSupervisor == -1) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo obtener el empleado vinculado al usuario de sesión.");
            return;
        }

        // Subservicio afectado (opcional)
        DetalleSubservicio subSel = cmbSubservicioAfectado.getValue();
        boolean tieneSubservicio  = subSel != null
                && !subSel.getIdDetalle().equals("0");

        // Llamar sp_RegistrarIncidencia
        String sql = "{call sp_RegistrarIncidencia(?, ?, ?, ?, ?)}";

        try (Connection conn = conexion.estabecerConexion();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt   (1, Sesion.idServicioPintura);
            if (tieneSubservicio) cs.setInt(2, Integer.parseInt(subSel.getIdDetalle()));
            else                  cs.setNull(2, Types.INTEGER);
            cs.setString(3, cmbTipoDefecto.getValue());
            cs.setString(4, txtDescripcion.getText().trim());
            cs.setInt   (5, idSupervisor);

            cs.execute();

            JOptionPane.showMessageDialog(null,
                    "Incidencia registrada. El servicio pasa a EN CORRECCIÓN.");
            Sesion.estadoServicio = "en_correccion";
            fnCancelar(event);   // Volver al detalle

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al registrar: " + e.getMessage());
        }
    }

    // ─── OBTENER ID EMPLEADO DESDE USUARIO ────────────────────────
    private int obtenerIdEmpleado(int idUsuario) {
        String sql = "SELECT fk_empleado FROM [tbl.Usuario] WHERE id_usuario = ?";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("fk_empleado");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error obteniendo empleado: " + e.getMessage());
        }
        return -1;
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