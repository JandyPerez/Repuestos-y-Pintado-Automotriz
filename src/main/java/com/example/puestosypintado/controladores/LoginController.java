package com.example.puestosypintado.controladores;

import com.example.puestosypintado.Database.Conexion;
import com.example.puestosypintado.util.SesionUsuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    Conexion conexion = new Conexion();

    @FXML private TextField     txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label         lblError;

    // ─── LOGIN ────────────────────────────────────────────────────    // ─── LOGIN ────────────────────────────────────────────────────
    @FXML
    public void fnLogin(ActionEvent event) {

        String usuario    = txtUsuario.getText().trim();
        String contrasena = txtPassword.getText().trim();

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            mostrarError("Complete usuario y contraseña.");
            return;
        }

        DatosLogin datos = verificarCredenciales(usuario, contrasena);

        if (datos == null) {
            mostrarError("Usuario o contraseña incorrectos.");
            txtPassword.clear();
            return;
        }

        if ("Inactivo".equalsIgnoreCase(datos.estado)) {
            mostrarError("Usuario inactivo. Contacte al administrador.");
            txtPassword.clear();
            return;
        }

        // ── Guardar sesión global ──────────────────────────────────
        SesionUsuario.instancia().iniciar(datos.nombreCompleto, datos.rol);

        abrirHome(event);
    }

        // ─── VERIFICAR CREDENCIALES ───────────────────────────────────
        // Devuelve null si no existe; DatosLogin con los campos si existe.    // Devuelve null si no existe; DatosLogin con los campos si existe.
        private DatosLogin verificarCredenciales(String usuario, String contrasena) {

            String sql = "SELECT u.estado, u.rol, e.nombre_completo " +
                    "FROM [tbl.Usuario] u " +
                    "JOIN [tbl.Empleado] e ON u.fk_empleado = e.id_empleado " +
                    "WHERE u.username = ? AND u.password_hash = ?";

            try (Connection conn = conexion.estabecerConexion();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, usuario);
                ps.setString(2, contrasena);

                ResultSet rs = ps.executeQuery();

                if (!rs.next()) return null;

                DatosLogin d = new DatosLogin();
                d.estado         = rs.getString("estado");
                d.rol            = rs.getString("rol");
                d.nombreCompleto = rs.getString("nombre_completo");
                return d;

            } catch (Exception e) {
                mostrarError("Error de conexión: " + e.getMessage());
                return null;
            }
        }

            // ─── ABRIR HOME ───────────────────────────────────────────────
            private void abrirHome(ActionEvent event) {
                try {
                    Parent root = FXMLLoader.load(getClass().getResource(
                            "/com/example/puestosypintado/General/Home.fxml"));
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Sistema de Gestión — Panel Principal");
                    stage.show();
                } catch (Exception e) {
                    mostrarError("Error al abrir el sistema: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // ─── MOSTRAR ERROR ────────────────────────────────────────────
            private void mostrarError(String mensaje) {
                lblError.setText(mensaje);
                lblError.setVisible(true);
            }

            // ─── DTO interno ─────────────────────────────────────────────
            private static class DatosLogin {
                String estado;
                String rol;
                String nombreCompleto;
            }
            }