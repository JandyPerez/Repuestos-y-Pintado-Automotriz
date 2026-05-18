package com.example.puestosypintado.controladores;

import com.example.puestosypintado.Database.Conexion;
import com.example.puestosypintado.util.SesionUsuario;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    Conexion conexion = new Conexion();

    // ─── FXML ────────────────────────────────────────────────────
    @FXML private TextField     txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label         lblError;
    @FXML private Label         lblIntentos;   // "Intentos restantes: X"
    @FXML private Label         lblBloqueo;    // "Intente de nuevo en Xs..."
    @FXML private Button        btnEntrar;

    // ─── Control de intentos ─────────────────────────────────────
    private static final int MAX_INTENTOS      = 3;
    private static final int SEGUNDOS_BLOQUEO  = 30;
    private int    intentosFallidos = 0;
    private boolean bloqueado       = false;
    private Timeline cuentaRegresiva;

    // ─── INITIALIZE ──────────────────────────────────────────────
    public void initialize() {

        // Limpiar error cuando el usuario empieza a escribir
        txtUsuario.textProperty().addListener((obs, old, val) -> ocultarError());
        txtPassword.textProperty().addListener((obs, old, val) -> ocultarError());

        // Foco automático en el campo de usuario al abrir la pantalla
        txtUsuario.requestFocus();
    }

    // ─── LOGIN ────────────────────────────────────────────────────
    @FXML
    public void fnLogin(ActionEvent event) {

        if (bloqueado) return;   // ignorar clicks durante bloqueo

        String usuario    = txtUsuario.getText().trim();
        String contrasena = txtPassword.getText().trim();

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            mostrarError("Complete usuario y contrasena.");
            return;
        }

        DatosLogin datos = verificarCredenciales(usuario, contrasena);

        // ── Credenciales incorrectas ──────────────────────────────
        if (datos == null) {
            intentosFallidos++;
            txtPassword.clear();

            int restantes = MAX_INTENTOS - intentosFallidos;

            if (intentosFallidos >= MAX_INTENTOS) {
                iniciarBloqueo();
            } else {
                mostrarError("Usuario o contrasena incorrectos.");
                mostrarIntentos(restantes);
            }
            return;
        }

        // ── Usuario inactivo ──────────────────────────────────────
        if ("Inactivo".equalsIgnoreCase(datos.estado)) {
            mostrarError("Usuario inactivo. Contacte al administrador.");
            txtPassword.clear();
            return;
        }

        // ── Login exitoso: guardar sesión y abrir Home ────────────
        SesionUsuario.instancia().iniciar(
                datos.nombreCompleto,
                datos.rol//,
                //usuario              // username también en sesión
        );

        intentosFallidos = 0;        // resetear contador al entrar
        abrirHome(event);
    }

    // ─── VERIFICAR CREDENCIALES ───────────────────────────────────
    private DatosLogin verificarCredenciales(String usuario, String contrasena) {

        // JOIN con Empleado para obtener el nombre completo
        String sql = "SELECT u.estado, u.rol, e.nombre_completo "
                + "FROM [tbl.Usuario] u "
                + "JOIN [tbl.Empleado] e ON u.fk_empleado = e.id_empleado "
                + "WHERE u.username = ? AND u.password_hash = ?";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, usuario);
            ps.setString(2, contrasena);

            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;

            DatosLogin d     = new DatosLogin();
            d.estado         = rs.getString("estado");
            d.rol            = rs.getString("rol");
            d.nombreCompleto = rs.getString("nombre_completo");
            return d;

        } catch (Exception e) {
            mostrarError("Error de conexion: " + e.getMessage());
            return null;
        }
    }

    // ─── BLOQUEO POR INTENTOS FALLIDOS ───────────────────────────
    // Deshabilita el botón y muestra cuenta regresiva de 30 segundos.
    private void iniciarBloqueo() {

        bloqueado = true;
        btnEntrar.setDisable(true);
        ocultarIntentos();
        mostrarError("Demasiados intentos fallidos.");

        // Referencia mutable para el lambda
        final int[] segundosRestantes = { SEGUNDOS_BLOQUEO };

        cuentaRegresiva = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    segundosRestantes[0]--;
                    lblBloqueo.setText(
                            "Acceso bloqueado. Intente de nuevo en "
                                    + segundosRestantes[0] + "s...");
                    lblBloqueo.setVisible(true);

                    if (segundosRestantes[0] <= 0) {
                        desbloquear();
                    }
                })
        );

        cuentaRegresiva.setCycleCount(SEGUNDOS_BLOQUEO);
        cuentaRegresiva.play();

        // Mostrar mensaje inicial de bloqueo inmediatamente
        lblBloqueo.setText("Acceso bloqueado. Intente de nuevo en "
                + SEGUNDOS_BLOQUEO + "s...");
        lblBloqueo.setVisible(true);
    }

    private void desbloquear() {
        bloqueado         = false;
        intentosFallidos  = 0;
        btnEntrar.setDisable(false);
        lblBloqueo.setVisible(false);
        lblBloqueo.setText("");
        ocultarError();
        txtPassword.clear();
        txtUsuario.requestFocus();
    }

    // ─── ABRIR HOME ───────────────────────────────────────────────
    private void abrirHome(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(
                    "/com/example/puestosypintado/General/Home.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Repuestos y Pintado — Panel Principal");
            stage.show();
        } catch (Exception e) {
            mostrarError("Error al abrir el sistema: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─── HELPERS DE UI ───────────────────────────────────────────
    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
    }

    private void ocultarError() {
        lblError.setText("");
        lblError.setVisible(false);
    }

    private void mostrarIntentos(int restantes) {
        String texto = restantes == 1
                ? "Ultimo intento antes del bloqueo."
                : "Intentos restantes: " + restantes;
        lblIntentos.setText(texto);
        lblIntentos.setStyle(restantes == 1
                ? "-fx-font-family: Courier New; -fx-font-size: 10px; -fx-text-fill: #CC0000; -fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;"
                : "-fx-font-family: Courier New; -fx-font-size: 10px; -fx-text-fill: #E65C00; -fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");
        lblIntentos.setVisible(true);
    }

    private void ocultarIntentos() {
        lblIntentos.setVisible(false);
        lblIntentos.setText("");
    }

    // ─── DTO interno ─────────────────────────────────────────────
    private static class DatosLogin {
        String estado;
        String rol;
        String nombreCompleto;
    }
}