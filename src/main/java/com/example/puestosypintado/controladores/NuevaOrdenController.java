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
import java.sql.*;
import java.time.LocalDate;

public class NuevaOrdenController {

    Conexion conexion = new Conexion();

    @FXML private TextField txtBuscarCliente;
    @FXML private TableView<Cliente> tvClientes;
    @FXML private TableColumn<Cliente, String> colClienteNombre;
    @FXML private TableColumn<Cliente, String> colClienteCedula;

    @FXML private TableView<Vehiculo> tvVehiculos;
    @FXML private TableColumn<Vehiculo, String> colVehiculoPlaca;
    @FXML private TableColumn<Vehiculo, String> colVehiculoModelo;

    @FXML private TextField txtClienteSeleccionado;
    @FXML private TextField txtIdCliente;
    @FXML private TextField txtVehiculoSeleccionado;
    @FXML private TextField txtIdVehiculo;

    @FXML private TextArea txtDescripcionTrabajo;
    @FXML private TextArea txtObservaciones;
    @FXML private TextField txtMontoCotizado;
    @FXML private TextField txtPorcentajeAnticipo;

    @FXML private DatePicker dpFechaPrometida;
    @FXML private DatePicker dpFechaLimitePago;
    @FXML private TextField  txtEstado;

    @FXML
    public void initialize() {

        colClienteNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colClienteCedula.setCellValueFactory(c -> c.getValue().cedulaProperty());
        colVehiculoPlaca.setCellValueFactory(c -> c.getValue().placaProperty());
        colVehiculoModelo.setCellValueFactory(c -> c.getValue().marcaModeloProperty());

        dpFechaLimitePago.setValue(LocalDate.now().plusDays(7));
        txtPorcentajeAnticipo.setText("40");

        tvClientes.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                txtClienteSeleccionado.setText(n.getNombre());
                txtIdCliente.setText(n.getId_cliente());
                cargarVehiculos(Integer.parseInt(n.getId_cliente()));
                txtVehiculoSeleccionado.clear();
                txtIdVehiculo.clear();
            }
        });

        tvVehiculos.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                txtVehiculoSeleccionado.setText(n.getPlaca() + " - " + n.getMarcaModelo());
                txtIdVehiculo.setText(String.valueOf(n.getIdVehiculo()));
            }
        });

        txtBuscarCliente.textProperty().addListener((obs, o, n) -> cargarClientes(n));

        cargarClientes("");
    }

    private void mostrarError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void mostrarInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void cargarClientes(String filtro) {
        ObservableList<Cliente> lista = FXCollections.observableArrayList();

        String sql = "SELECT id_cliente, nombre, cedula, telefono " +
                "FROM tbl.Cliente " +
                "WHERE estado='ACTIVO' AND (nombre LIKE ? OR cedula LIKE ?)";

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
                        rs.getString("telefono")
                ));
            }

            tvClientes.setItems(lista);

        } catch (Exception e) {
            mostrarError("Error cargando clientes: " + e.getMessage());
        }
    }

    private void cargarVehiculos(int idCliente) {

        ObservableList<Vehiculo> lista = FXCollections.observableArrayList();

        String sql = "SELECT id_vehiculo, placa, " +
                "marca + ' ' + modelo + ' (' + CAST([año] AS VARCHAR) + ')' AS marca_modelo " +
                "FROM tbl.Vehiculo WHERE fk_cliente=?";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idCliente);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(new Vehiculo(
                        rs.getString("id_vehiculo"),
                        rs.getString("placa"),
                        rs.getString("marca_modelo") // ✔ FIX
                ));
            }

            tvVehiculos.setItems(lista);

        } catch (Exception e) {
            mostrarError("Error cargando vehículos: " + e.getMessage());
        }
    }

    @FXML
    public void fnGuardarOrden() {

        if (txtIdCliente.getText().isEmpty()) {
            mostrarError("Seleccione cliente");
            return;
        }

        if (txtIdVehiculo.getText().isEmpty()) {
            mostrarError("Seleccione vehículo");
            return;
        }

        if (txtDescripcionTrabajo.getText().trim().isEmpty()) {
            mostrarError("Ingrese descripción");
            return;
        }

        double monto = 0;
        try {
            if (!txtMontoCotizado.getText().isEmpty())
                monto = Double.parseDouble(txtMontoCotizado.getText());

            if (monto < 0) throw new Exception();

        } catch (Exception e) {
            mostrarError("Monto inválido");
            return;
        }

        double anticipo;
        try {
            String txt = txtPorcentajeAnticipo.getText();
            anticipo = txt.isEmpty() ? 40 : Double.parseDouble(txt);
        } catch (Exception e) {
            mostrarError("Anticipo inválido");
            return;
        }

        if (dpFechaPrometida.getValue() != null &&
                dpFechaPrometida.getValue().isBefore(LocalDate.now())) {
            mostrarError("Fecha prometida inválida");
            return;
        }

        String sql = "INSERT INTO tbl.Servicio_Pintura " +
                "(fk_cliente,fk_vehiculo,fk_usuario,descripcion_trabajo,estado," +
                "monto_cotizado,fecha_entrada,fecha_prometida,observaciones,porcentaje_anticipo,fecha_limite_pago)" +
                " VALUES (?,?,?,?, 'Cotizado', ?, GETDATE(), ?, ?, ?, ?)";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, Integer.parseInt(txtIdCliente.getText()));
            ps.setInt(2, Integer.parseInt(txtIdVehiculo.getText()));
            ps.setInt(3, Sesion.idUsuario);
            ps.setString(4, txtDescripcionTrabajo.getText());
            ps.setDouble(5, monto);

            if (dpFechaPrometida.getValue() != null)
                ps.setDate(6, Date.valueOf(dpFechaPrometida.getValue()));
            else
                ps.setNull(6, Types.DATE);

            ps.setString(7, txtObservaciones.getText());
            ps.setDouble(8, anticipo);
            ps.setDate(9, Date.valueOf(dpFechaLimitePago.getValue()));

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                Sesion.idServicioPintura = rs.getInt(1);
            }

            mostrarInfo("Orden creada correctamente");
            limpiar();

        } catch (Exception e) {
            mostrarError("Error: " + e.getMessage());
        }
    }

    private void limpiar() {
        txtDescripcionTrabajo.clear();
        txtObservaciones.clear();
        txtMontoCotizado.clear();
        txtPorcentajeAnticipo.setText("40");
        dpFechaPrometida.setValue(null);
        dpFechaLimitePago.setValue(LocalDate.now().plusDays(7));
    }

    @FXML
    public void fnLimpiar(ActionEvent event) {
        limpiar();
    }

    @FXML
    public void fnBuscarCliente(ActionEvent event) {
        cargarClientes(txtBuscarCliente.getText());
    }

    @FXML
    public void fnIrSubservicios(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(
                    "/com/example/puestosypintado/Pintura/Subservicios.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            mostrarError("Error al abrir subservicios: " + e.getMessage());
        }
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
            mostrarError("Error al volver al inicio: " + e.getMessage());
        }
    }
}