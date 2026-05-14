package com.example.puestosypintado.controladores;

import com.example.puestosypintado.util.SesionUsuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HomeController {

    public VBox vboxMenu;
    // ── Labels del header ────────────────────────────────────────
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;

    // ── Botones del menú lateral ─────────────────────────────────
    @FXML private Button btnClientes;
    @FXML private Button btnEmpleados;
    @FXML private Button btnProductos;
    @FXML private Button btnProveedores;
    @FXML private Button btnUsuarios;
    @FXML private Button btnVehiculos;
    @FXML private Button btnVentas;
    @FXML private Button btnPagos;
    @FXML private Button btnPintura;

    // ─── initialize ──────────────────────────────────────────────
    @FXML
    public void initialize() {

        SesionUsuario sesion = SesionUsuario.instancia();

        // Mostrar nombre y rol en el header
        lblNombreUsuario.setText(sesion.getNombre());
        lblRolUsuario.setText(sesion.getRol());

        // Aplicar visibilidad según rol
        configurarMenuPorRol(sesion.getRol());
    }

    // ─── CONFIGURAR MENÚ POR ROL ─────────────────────────────────
    /**
     * Cada rol solo ve los módulos que le corresponden.
     *
     * Administrador  → todo
     * Recepcionista  → Clientes, Vehículos, Servicio Pintura
     * Mecánico       → Vehículos, Servicio Pintura
     * Cajero         → Ventas, Pagos
     * Vendedor       → Ventas, Productos
     * Técnico        → Vehículos, Servicio Pintura, Productos
     */
    private void configurarMenuPorRol(String rol) {

        // Ocultar todo primero
        ocultarTodos();

        switch (rol) {

            case "Admin":
                mostrarTodos();
                break;

            case "Recepcionista":
                mostrar(btnClientes, btnVehiculos, btnPintura);
                break;

            case "Mecánico":
                mostrar(btnVehiculos, btnPintura);
                break;

            case "Cajero":
                mostrar(btnVentas, btnPagos);
                break;

            case "Vendedor":
                mostrar(btnVentas, btnProductos);
                break;

            case "Técnico":
                mostrar(btnVehiculos, btnPintura, btnProductos);
                break;

            default:
                //Rol desconocido → no mostrar nada (solo el botón de salida)
                break;
        }
    }

    // ── Helpers de visibilidad ───────────────────────────────────
    private void ocultarTodos() {
        Button[] todos = { btnClientes, btnEmpleados, btnProductos,
                btnProveedores, btnUsuarios, btnVehiculos,
                btnVentas, btnPagos, btnPintura };
        for (Button b : todos) { b.setVisible(false); b.setManaged(false); }
    }

    private void mostrarTodos() {
        Button[] todos = { btnClientes, btnEmpleados, btnProductos,
                btnProveedores, btnUsuarios, btnVehiculos,
                btnVentas, btnPagos, btnPintura };
        for (Button b : todos) { b.setVisible(true); b.setManaged(true); }
    }

    private void mostrar(Button... botones) {
        for (Button b : botones) { b.setVisible(true); b.setManaged(true); }
    }

    // ─── NAVEGACIÓN ──────────────────────────────────────────────
    private void cambiarEscena(ActionEvent event, String rutaFXML) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(rutaFXML));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML public void irCliente(ActionEvent event) {
        cambiarEscena(event, "/com/example/puestosypintado/General/Cliente.fxml");
    }
    @FXML public void irEmpleado(ActionEvent event) {
        cambiarEscena(event, "/com/example/puestosypintado/General/Empleado.fxml");
    }
    @FXML public void irProducto(ActionEvent event) {
        cambiarEscena(event, "/com/example/puestosypintado/Venta/Producto.fxml");
    }
    @FXML public void irProveedor(ActionEvent event) {
        cambiarEscena(event, "/com/example/puestosypintado/Venta/Proveedor.fxml");
    }
    @FXML public void irUsuario(ActionEvent event) {
        cambiarEscena(event, "/com/example/puestosypintado/General/Usuario.fxml");
    }
    @FXML public void irVehiculo(ActionEvent event) {
        cambiarEscena(event, "/com/example/puestosypintado/Venta/Vehiculo.fxml");
    }
    @FXML public void irVenta(ActionEvent event) {
        cambiarEscena(event, "/com/example/puestosypintado/General/Venta.fxml");
    }
    @FXML public void irPago(ActionEvent event) {
        cambiarEscena(event, "/com/example/puestosypintado/General/Pago.fxml");
    }
    @FXML public void irPintura(ActionEvent event) {
        cambiarEscena(event, "/com/example/puestosypintado/Pintura/DetalleServicio.fxml");
    }

    // ─── VOLVER AL LOGIN ─────────────────────────────────────────
    @FXML
    public void irLogin(ActionEvent event) {
        SesionUsuario.instancia().cerrar();   // limpiar sesión
        cambiarEscena(event, "/com/example/puestosypintado/General/Login.fxml");
    }
}