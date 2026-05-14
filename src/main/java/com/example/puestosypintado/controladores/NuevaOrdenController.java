package com.example.puestosypintado.controladores;

import com.example.puestosypintado.Database.Conexion;
import com.example.puestosypintado.modelo.Cliente;
import com.example.puestosypintado.modelo.Vehiculo;
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
import java.time.LocalDate;

/**
 * Controlador: Pantalla 2 — Nueva Orden de Servicio de Pintura
 * INSERT → [tbl.Servicio_Pintura]
 */
public class NuevaOrdenController {

    Conexion conexion = new Conexion();

    // ─── FXML — Panel izquierdo (búsqueda) ───────────────────────
    @FXML private TextField                            txtBuscarCliente;
    @FXML private TableView<Cliente>             tvClientes;
    @FXML private TableColumn<Cliente, String>   colClienteNombre;
    @FXML private TableColumn<Cliente, String>   colClienteCedula;
    @FXML private TableView<Vehiculo>            tvVehiculos;
    @FXML private TableColumn<Vehiculo, String>  colVehiculoPlaca;
    @FXML private TableColumn<Vehiculo, String>  colVehiculoModelo;

    // ─── FXML — Campos de orden ───────────────────────────────────
    @FXML private TextField  txtClienteSeleccionado;
    @FXML private TextField  txtIdCliente;
    @FXML private TextField  txtVehiculoSeleccionado;
    @FXML private TextField  txtIdVehiculo;
    @FXML private TextArea   txtDescripcionTrabajo;
    @FXML private TextArea   txtObservaciones;
    @FXML private TextField  txtMontoCotizado;
    @FXML private TextField  txtPorcentajeAnticipo;
    @FXML private DatePicker dpFechaPrometida;
    @FXML private DatePicker dpFechaLimitePago;
    @FXML private TextField  txtEstado;

    // ─── INITIALIZE ──────────────────────────────────────────────
    public void initialize() {

        colClienteNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colClienteCedula.setCellValueFactory(c -> c.getValue().cedulaProperty());
        colVehiculoPlaca.setCellValueFactory(c -> c.getValue().placaProperty());
        colVehiculoModelo.setCellValueFactory(c -> c.getValue().marcaModeloProperty());

        // Límite de anticipo: hoy + 7 días por defecto
        dpFechaLimitePago.setValue(LocalDate.now().plusDays(7));

        // Al seleccionar cliente → cargar sus vehículos
        tvClientes.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        txtClienteSeleccionado.setText(newVal.getNombre());
                        txtIdCliente.setText(newVal.getId_cliente());
                        cargarVehiculos(Integer.parseInt(newVal.getId_cliente()));
                        // Limpiar vehículo anterior
                        txtVehiculoSeleccionado.clear();
                        txtIdVehiculo.clear();
                    }
                });

        // Al seleccionar vehículo → rellenar campos
        tvVehiculos.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        txtVehiculoSeleccionado.setText(
                                newVal.getPlaca() + " — " + newVal.getMarcaModelo());
                        txtIdVehiculo.setText(String.valueOf(newVal.getIdVehiculo()));                    }
                });

        // Buscar mientras se escribe
        txtBuscarCliente.textProperty().addListener(
                (obs, oldVal, newVal) -> cargarClientes(newVal));

        cargarClientes("");
    }

    // ─── CARGAR CLIENTES ─────────────────────────────────────────
    private void cargarClientes(String filtro) {
        ObservableList<Cliente> lista = FXCollections.observableArrayList();

        String sql = "SELECT id_cliente, nombre, cedula, telefono "
                + "FROM [tbl.Cliente] "
                + "WHERE estado = 'ACTIVO' "
                + "  AND (nombre LIKE ? OR cedula LIKE ?) "
                + "ORDER BY nombre";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String like = "%" + filtro + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(new Cliente(
                        rs.getString("id_cliente"),
                        rs.getString("nombre"),
                        rs.getString("cedula"),
                        rs.getString("telefono") == null ? "" : rs.getString("telefono")
                ));
            }
            tvClientes.setItems(lista);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error cargando clientes: " + e.getMessage());
        }
    }

    // ─── CARGAR VEHÍCULOS POR CLIENTE ────────────────────────────
    private void cargarVehiculos(int idCliente) {
        ObservableList<Vehiculo> lista = FXCollections.observableArrayList();

        String sql = "SELECT id_vehiculo, placa, marca + ' ' + modelo + ' (' + CAST([año] AS VARCHAR) + ')' "
                + "       AS marca_modelo "
                + "FROM [tbl.Vehiculo] "
                + "WHERE fk_cliente = ? "
                + "ORDER BY placa";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idCliente);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(new Vehiculo(
                        rs.getString("id_vehiculo"),
                        rs.getString("placa"),
                        rs.getString("marca")
                ));
            }
            tvVehiculos.setItems(lista);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error cargando vehículos: " + e.getMessage());
        }
    }

    // ─── BUSCAR (botón ?) ─────────────────────────────────────────
    @FXML
    public void fnBuscarCliente(ActionEvent event) {
        cargarClientes(txtBuscarCliente.getText().trim());
    }

    // ─── GUARDAR ORDEN ────────────────────────────────────────────
    @FXML
    public void fnGuardarOrden(ActionEvent event) {

        // Validaciones
        if (txtIdCliente.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Seleccione un cliente de la lista.");
            return;
        }
        if (txtIdVehiculo.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Seleccione un vehículo del cliente.");
            return;
        }
        if (txtDescripcionTrabajo.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "La descripción del trabajo es obligatoria.");
            return;
        }
        if (Sesion.idUsuario == 0) {
            JOptionPane.showMessageDialog(null, "No hay usuario de sesión activo.");
            return;
        }

        // Monto cotizado
        double montoCotizado = 0.0;
        String montoStr = txtMontoCotizado.getText().trim();
        if (!montoStr.isEmpty()) {
            try {
                montoCotizado = Double.parseDouble(montoStr);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Monto cotizado debe ser un número.");
                return;
            }
        }

        // Porcentaje anticipo
        double pctAnticipo = 40.0;
        try {
            pctAnticipo = Double.parseDouble(txtPorcentajeAnticipo.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Porcentaje de anticipo inválido.");
            return;
        }

        // Fechas
        java.sql.Date fechaPrometida = dpFechaPrometida.getValue() != null
                ? java.sql.Date.valueOf(dpFechaPrometida.getValue()) : null;
        java.sql.Date fechaLimite = dpFechaLimitePago.getValue() != null
                ? java.sql.Date.valueOf(dpFechaLimitePago.getValue())
                : java.sql.Date.valueOf(LocalDate.now().plusDays(7));

        String sql = "INSERT INTO [tbl.Servicio_Pintura] "
                + "(fk_cliente, fk_vehiculo, fk_usuario, descripcion_trabajo, "
                + " estado, monto_cotizado, fecha_entrada, fecha_prometida, "
                + " observaciones, porcentaje_anticipo, fecha_limite_pago) "
                + "VALUES (?, ?, ?, ?, 'cotizado', ?, GETDATE(), ?, ?, ?, ?)";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt   (1, Integer.parseInt(txtIdCliente.getText()));
            ps.setInt   (2, Integer.parseInt(txtIdVehiculo.getText()));
            ps.setInt   (3, Sesion.idUsuario);
            ps.setString(4, txtDescripcionTrabajo.getText().trim());
            ps.setDouble(5, montoCotizado);

            if (fechaPrometida != null) ps.setDate(6, fechaPrometida);
            else                        ps.setNull(6, Types.DATE);

            ps.setString(7, txtObservaciones.getText().trim());
            ps.setDouble(8, pctAnticipo);
            ps.setDate  (9, fechaLimite);

            if (ps.executeUpdate() == 1) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    Sesion.idServicioPintura = keys.getInt(1);
                    Sesion.estadoServicio    = "cotizado";
                }
                JOptionPane.showMessageDialog(null,
                        "Orden #" + Sesion.idServicioPintura + " creada correctamente.\n"
                                + "Ahora agregue los subservicios.");
                fnLimpiar(null);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al guardar: " + e.getMessage());
        }
    }

    // ─── IR A SUBSERVICIOS ────────────────────────────────────────
    @FXML
    public void fnIrSubservicios(ActionEvent event) {
        if (Sesion.idServicioPintura == 0) {
            JOptionPane.showMessageDialog(null,
                    "Primero guarde la orden antes de agregar subservicios.");
            return;
        }
        try {
            Parent root = FXMLLoader.load(getClass().getResource(
                    "/com/example/puestosypintado/pintura/AgregarSubservicios.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─── LIMPIAR ─────────────────────────────────────────────────
    @FXML
    public void fnLimpiar(ActionEvent event) {
        txtBuscarCliente.clear();
        txtClienteSeleccionado.clear();
        txtIdCliente.clear();
        txtVehiculoSeleccionado.clear();
        txtIdVehiculo.clear();
        txtDescripcionTrabajo.clear();
        txtObservaciones.clear();
        txtMontoCotizado.clear();
        txtPorcentajeAnticipo.setText("40");
        dpFechaPrometida.setValue(null);
        dpFechaLimitePago.setValue(LocalDate.now().plusDays(7));
        tvClientes.getSelectionModel().clearSelection();
        tvVehiculos.getSelectionModel().clearSelection();
        tvVehiculos.getItems().clear();
        cargarClientes("");
    }

    @FXML
    public void irHome(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(
                    "/com/example/puestosypintado/General/Home.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}