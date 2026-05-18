package com.example.puestosypintado.controladores;

import com.example.puestosypintado.Database.Conexion;
import com.example.puestosypintado.util.Sesion;
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
 * Controlador: Pantalla 7 — Resolver Incidencia
 * UPDATE → [tbl.Incidencias_Servicio] via sp_ResolverIncidencia
 *          estado: 'abierta'/'en_correccion' → 'resuelta'
 * READ   → [tbl.Incidencias_Servicio] (datos de la incidencia, solo lectura)
 * READ   → [tbl.Empleado] via Sesion.idUsuario (supervisor)
 * Efecto → si no quedan incidencias abiertas, servicio vuelve a 'terminado'
 */
public class ResolverIncidenciaController {

    Conexion conexion = new Conexion();

    // ─── FXML — Contexto de la incidencia ────────────────────────
    @FXML private TextField txtIdIncidencia;
    @FXML private TextField txtIdOrdenCtx;
    @FXML private TextField txtEstadoIncidencia;

    // ─── FXML — Datos del defecto (solo lectura) ──────────────────
    @FXML private TextField txtTipoDefecto;
    @FXML private TextField txtSubservicioAfectado;
    @FXML private TextArea  txtDescripcionOriginal;

    // ─── FXML — Resolución ───────────────────────────────────────
    @FXML private TextArea  txtObservacionResolucion;
    @FXML private TextField txtSupervisor;
    @FXML private TextField txtIdSupervisor;

    // ─── INITIALIZE ──────────────────────────────────────────────
    public void initialize() {
        cargarIncidencia();
        cargarSupervisor();
    }

    // ─── CARGAR DATOS DE LA INCIDENCIA ───────────────────────────
    private void cargarIncidencia() {
        if (Sesion.idIncidencia == 0) return;

        String sql =
                "SELECT i.id_incidencia, i.fk_servicioPintura, i.estado, "
                        + "       i.tipo_defecto, i.descripcion, "
                        + "       ISNULL(sb.nombre, '--') AS subservicio "
                        + "FROM [tbl.Incidencias_Servicio] i "
                        + "LEFT JOIN [tbl.Detalle_Servicio_Pintura] d "
                        + "       ON i.fk_detalleServicio = d.id_detalleServicio "
                        + "LEFT JOIN [tbl.Subservicios] sb ON d.fk_subservicio = sb.id_subservicio "
                        + "WHERE i.id_incidencia = ?";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Sesion.idIncidencia);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                txtIdIncidencia.setText      (rs.getString("id_incidencia"));
                txtIdOrdenCtx.setText        (rs.getString("fk_servicioPintura"));
                txtEstadoIncidencia.setText  (rs.getString("estado").toUpperCase());
                txtTipoDefecto.setText       (rs.getString("tipo_defecto"));
                txtSubservicioAfectado.setText(rs.getString("subservicio"));
                txtDescripcionOriginal.setText(rs.getString("descripcion"));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error cargando incidencia: " + e.getMessage());
        }
    }

    // ─── SUPERVISOR (sesión activa) ───────────────────────────────
    private void cargarSupervisor() {
        if (Sesion.idUsuario == 0) return;

        String sql =
                "SELECT e.nombre_completo, e.id_empleado "
                        + "FROM [tbl.Empleado] e "
                        + "JOIN [tbl.Usuario]  u ON e.id_empleado = u.fk_empleado "
                        + "WHERE u.id_usuario = ?";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Sesion.idUsuario);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtSupervisor.setText  (rs.getString("nombre_completo"));
                txtIdSupervisor.setText(rs.getString("id_empleado"));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error cargando supervisor: " + e.getMessage());
        }
    }

    // ─── RESOLVER INCIDENCIA ─────────────────────────────────────
    @FXML
    public void fnResolverIncidencia(ActionEvent event) {

        if (txtObservacionResolucion.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Describe la resolución antes de marcar como resuelta.");
            return;
        }
        if (txtIdSupervisor.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay supervisor de sesión activo.");
            return;
        }

        int idSupervisor = Integer.parseInt(txtIdSupervisor.getText().trim());

        // Llamar sp_ResolverIncidencia
        String sql = "{call sp_ResolverIncidencia(?, ?, ?)}";

        try (Connection conn = conexion.estabecerConexion();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt   (1, Sesion.idIncidencia);
            cs.setInt   (2, idSupervisor);
            cs.setString(3, txtObservacionResolucion.getText().trim());
            cs.execute();

            // Verificar si el servicio volvió a 'terminado'
            String nuevoEstado = consultarEstadoServicio();
            Sesion.estadoServicio = nuevoEstado;

            String msg = "Incidencia resuelta correctamente.";
            if ("terminado".equals(nuevoEstado)) {
                msg += "\nNo quedan incidencias abiertas — el servicio volvió a TERMINADO.";
            }
            JOptionPane.showMessageDialog(null, msg);

            Sesion.idIncidencia = 0;
            fnCancelar(event);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al resolver: " + e.getMessage());
        }
    }

    // Consulta el estado actual del servicio tras resolver
    private String consultarEstadoServicio() {
        String sql = "SELECT estado FROM [tbl.Servicio_Pintura] "
                + "WHERE id_servicioPintura = ?";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Sesion.idServicioPintura);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("estado");

        } catch (Exception e) {
            // Si falla la consulta, no interrumpir el flujo
        }
        return Sesion.estadoServicio;
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