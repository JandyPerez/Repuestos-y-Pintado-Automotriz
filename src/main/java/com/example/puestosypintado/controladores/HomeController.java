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

    // ── Labels de Secciones ──────────────────────────────────────
    @FXML private Label lblSecGeneral;
    @FXML private Label lblSecVentas;
    @FXML private Label lblSecTaller;

    // ── Botones del menú lateral ─────────────────────────────────
    @FXML private Button btnClientes;
    @FXML private Button btnEmpleados;
    @FXML private Button btnProductos;
    @FXML private Button btnProveedores;
    @FXML private Button btnUsuarios;
    @FXML private Button btnVehiculos;
    @FXML private Button btnVentas;
    @FXML private Button btnPagos;
    @FXML private Button btnAgregarSubservicio;
    @FXML private Button btnDetalleServicio;
    @FXML private Button btnEntregaVehiculo;
    @FXML private Button btnIncidenciasAbiertas;
    @FXML private Button btnNuevaOrden;
    @FXML private Button btnRegistrarIncidencia;

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
     * Recepcionista  → Clientes, Vehículos, Nueva Orden, Entrega, Detalle Servicio
     * Mecánico       → Vehículos, Detalle Servicio, Agregar Subservicio, Incidencias
     * Cajero         → Clientes, Ventas, Pagos
     * Vendedor       → Clientes, Productos, Proveedores, Ventas
     * Técnico pintura→ Vehículos, Productos, Detalle Servicio, Agregar Subservicio, Incidencias
     */
    private void configurarMenuPorRol(String rol) {

        // Ocultar todo primero
        ocultarTodos();

        switch (rol) {

            case "Admin":
                mostrarTodos();
                if (lblSecGeneral != null) { lblSecGeneral.setVisible(true); lblSecGeneral.setManaged(true); }
                if (lblSecVentas != null) { lblSecVentas.setVisible(true); lblSecVentas.setManaged(true); }
                if (lblSecTaller != null) { lblSecTaller.setVisible(true); lblSecTaller.setManaged(true); }
                break;

            case "Recepcionista":
                mostrar(btnClientes, btnVehiculos, btnNuevaOrden, btnDetalleServicio, btnEntregaVehiculo);
                break;

            case "Mecánico":
                mostrar(btnVehiculos, btnDetalleServicio, btnAgregarSubservicio, btnRegistrarIncidencia, btnIncidenciasAbiertas);
                break;

            case "Cajero":
                mostrar(btnClientes, btnVentas, btnPagos);
                break;

            case "Vendedor":
                mostrar(btnClientes, btnProductos, btnProveedores, btnVentas);
                break;

            case "Técnico pintura":
                mostrar(btnVehiculos, btnProductos, btnDetalleServicio, btnAgregarSubservicio, btnRegistrarIncidencia, btnIncidenciasAbiertas);
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
                btnVentas, btnPagos, btnAgregarSubservicio, btnDetalleServicio,
                btnEntregaVehiculo, btnIncidenciasAbiertas, btnNuevaOrden, btnRegistrarIncidencia };
        for (Button b : todos) { b.setVisible(false); b.setManaged(false); }

        if (lblSecGeneral != null) { lblSecGeneral.setVisible(false); lblSecGeneral.setManaged(false); }
        if (lblSecVentas != null) { lblSecVentas.setVisible(false); lblSecVentas.setManaged(false); }
        if (lblSecTaller != null) { lblSecTaller.setVisible(false); lblSecTaller.setManaged(false); }
    }

    private void mostrarTodos() {
        Button[] todos = { btnClientes, btnEmpleados, btnProductos,
                btnProveedores, btnUsuarios, btnVehiculos,
                btnVentas, btnPagos, btnAgregarSubservicio, btnDetalleServicio,
                btnEntregaVehiculo, btnIncidenciasAbiertas, btnNuevaOrden, btnRegistrarIncidencia };
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
        cambiarEscena(event, "/com/example/puestosypintado/Venta/Venta.fxml");
    }
    @FXML public void irPago(ActionEvent event) {
        cambiarEscena(event, "/com/example/puestosypintado/Venta/Pago.fxml");
    }
    @FXML public void irAgregarSubservicio(ActionEvent event) {
        cambiarEscena(event, "/com/example/puestosypintado/Pintura/AgregarSubservicio.fxml");
    }
    @FXML public void irDetalleServicio(ActionEvent event) {
        cambiarEscena(event, "/com/example/puestosypintado/Pintura/DetalleServicio.fxml");
    }
    @FXML public void irEntregaVehiculo(ActionEvent event) {
        cambiarEscena(event, "/com/example/puestosypintado/Pintura/EntregaVehiculo.fxml");
    }
    @FXML public void irIncidenciasAbiertas(ActionEvent event) {
        cambiarEscena(event, "/com/example/puestosypintado/Pintura/IncidenciasAbiertas.fxml");
    }
    @FXML public void irNuevaOrden(ActionEvent event) {
        cambiarEscena(event, "/com/example/puestosypintado/Pintura/NuevaOrden.fxml");
    }
    @FXML public void irRegistrarIncidencia(ActionEvent event) {
        cambiarEscena(event, "/com/example/puestosypintado/Pintura/RegistrarIncidencia.fxml");
    }

    // ─── VOLVER AL LOGIN ─────────────────────────────────────────
    @FXML
    public void irLogin(ActionEvent event) {
        SesionUsuario.instancia().cerrar();   // limpiar sesión
        cambiarEscena(event, "/com/example/puestosypintado/General/Login.fxml");
    }
}