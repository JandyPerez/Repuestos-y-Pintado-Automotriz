package com.example.puestosypintado.controladores;

import com.example.puestosypintado.Database.Conexion;
import com.example.puestosypintado.modelo.Vehiculo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javax.swing.JOptionPane;
import java.sql.*;
import java.util.Optional;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class VehiculoController {

    Conexion conexion = new Conexion();

    // ─── FXML ────────────────────────────────────────────────────
    @FXML private TextField        txtIdVehiculo;
    @FXML private TextField        txtMarca;
    @FXML private TextField        txtModelo;
    @FXML private TextField        txtAno;
    @FXML private TextField        txtColor;
    @FXML private TextField        txtPlaca;
    @FXML private TextField        txtVIN;
    @FXML private TextArea         txtNotas;
    @FXML private ComboBox<String> cmbCliente;

    @FXML private TableView<Vehiculo>           tvVehiculos;
    @FXML private TableColumn<Vehiculo, String> colPlaca;
    @FXML private TableColumn<Vehiculo, String> colMarcaModelo;

    // ─── INITIALIZE ──────────────────────────────────────────────
    public void initialize() {

        colPlaca.setCellValueFactory(
                c -> c.getValue().placaProperty());
        colMarcaModelo.setCellValueFactory(
                c -> c.getValue().marcaModeloProperty());

        cargarClientes();
        actualizarLista();

        tvVehiculos.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        buscarPorPlaca(newVal.getPlaca());
                    }
                });
    }

    // ─── CARGAR CLIENTES EN COMBOBOX ─────────────────────────────
    private void cargarClientes() {

        ObservableList<String> items = FXCollections.observableArrayList();

        String sql = "SELECT id_cliente, cedula, nombre FROM [tbl.Cliente] ORDER BY nombre";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                items.add(rs.getInt("id_cliente")
                        + " - "
                        + rs.getString("cedula")
                        + " "
                        + rs.getString("nombre"));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error al cargar clientes: " + e.getMessage());
        }

        cmbCliente.setItems(items);
    }

    private int getIdClienteSeleccionado() {
        String valor = cmbCliente.getValue();
        if (valor == null || valor.isBlank()) return -1;
        try {
            return Integer.parseInt(valor.split(" - ")[0].trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // ─── LISTA (TableView) ───────────────────────────────────────
    protected ObservableList<Vehiculo> observableVehiculo() {

        ObservableList<Vehiculo> lista = FXCollections.observableArrayList();

        String sql = "SELECT placa, CONCAT(marca,' ',modelo) AS MarcaModelo "
                + "FROM [tbl.Vehiculo]";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Vehiculo(
                        rs.getString("placa"),
                        rs.getString("MarcaModelo")));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error al cargar lista: " + e.getMessage());
        }

        return lista;
    }

    public void actualizarLista() {
        tvVehiculos.setItems(observableVehiculo());
    }

    // ═══════════════════════════════════════════════════════════════
    //  AÑADIDO — VehiculoBasico
    //  Retorna vehículos en formato VehiculoBasico, lista para usar
    //  en paneles de selección (NuevaOrdenController, etc.)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Retorna todos los vehículos de un cliente en formato VehiculoBasico.
     * Úsalo en el panel de selección de vehículo de NuevaOrdenController
     * al seleccionar un cliente en la tabla.
     *
     * @param idCliente fk_cliente exacto
     */
    public ObservableList<Vehiculo> observableVehiculoPorCliente(int idCliente) {

        ObservableList<Vehiculo> lista = FXCollections.observableArrayList();

        String sql = "SELECT id_vehiculo, placa, "
                + "       marca + ' ' + modelo + ' (' + CAST([año] AS VARCHAR) + ')' "
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
                        rs.getString("idVehiculo"),
                        rs.getString("placa"),
                        rs.getString("marca_modelo")
                ));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error al cargar vehículos: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Retorna todos los vehículos del sistema en formato VehiculoBasico,
     * sin filtrar por cliente.
     * Útil para buscadores generales.
     */
    public ObservableList<Vehiculo> observableVehiculoBasico() {

        ObservableList<Vehiculo> lista = FXCollections.observableArrayList();

        String sql = "SELECT id_vehiculo, placa, "
                + "       marca + ' ' + modelo + ' (' + CAST([año] AS VARCHAR) + ')' "
                + "       AS marca_modelo "
                + "FROM [tbl.Vehiculo] "
                + "ORDER BY placa";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Vehiculo(
                        rs.getString("id_vehiculo"),
                        rs.getString("placa"),
                        rs.getString("marca_modelo")
                ));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error al cargar vehículos: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Busca un vehículo por id y lo retorna como VehiculoBasico.
     * Retorna null si no existe.
     *
     * @param idVehiculo id_vehiculo exacto
     */
    public Vehiculo buscarVehiculoBasicoPorId(int idVehiculo) {

        String sql = "SELECT id_vehiculo, placa, "
                + "       marca + ' ' + modelo + ' (' + CAST([año] AS VARCHAR) + ')' "
                + "       AS marca_modelo "
                + "FROM [tbl.Vehiculo] WHERE id_vehiculo = ?";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idVehiculo);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Vehiculo(
                        rs.getString("id_vehiculo"),
                        rs.getString("placa"),
                        rs.getString("marca_modelo")
                );
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error al buscar vehículo: " + e.getMessage());
        }

        return null;
    }

    // ═══════════════════════════════════════════════════════════════
    //  FIN AÑADIDO — VehiculoBasico
    // ═══════════════════════════════════════════════════════════════

    // ─── GUARDAR ─────────────────────────────────────────────────
    @FXML
    protected void fnGuardarVehiculo(ActionEvent event) {

        String placa     = txtPlaca.getText().trim();
        String marca     = txtMarca.getText().trim();
        String vin       = txtVIN.getText().trim();
        int    idCliente = getIdClienteSeleccionado();

        if (idCliente == -1 || placa.isEmpty() || marca.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Cliente, Marca y Placa son obligatorios.");
            return;
        }

        if (existeVehiculo(placa, vin)) {
            JOptionPane.showMessageDialog(null,
                    "Ya existe un vehículo con esa Placa o VIN.");
            return;
        }

        String sql = "INSERT INTO [tbl.Vehiculo] "
                + "(fk_cliente, marca, modelo, [año], color, placa, vin, observaciones) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt   (1, idCliente);
            ps.setString(2, marca);
            ps.setString(3, txtModelo.getText().trim());

            String anoStr = txtAno.getText().trim();
            if (!anoStr.isEmpty()) {
                ps.setInt(4, Integer.parseInt(anoStr));
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            ps.setString(5, txtColor.getText().trim());
            ps.setString(6, placa);
            ps.setString(7, vin.isEmpty() ? null : vin);
            ps.setString(8, txtNotas.getText().trim());

            if (ps.executeUpdate() == 1) {
                JOptionPane.showMessageDialog(null,
                        "Vehículo guardado correctamente.");
                actualizarLista();
                fnLimpiar(null);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "El campo AÑO debe contener solo números.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Error al guardar: " + e.getMessage());
        }
    }

    private boolean existeVehiculo(String placa, String vin) {

        String sql = "SELECT id_vehiculo FROM [tbl.Vehiculo] "
                + "WHERE placa = ? OR (vin IS NOT NULL AND vin = ?)";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, placa);
            ps.setString(2, vin.isEmpty() ? null : vin);
            return ps.executeQuery().next();

        } catch (SQLException e) {
            return false;
        }
    }

    // ─── MODIFICAR ───────────────────────────────────────────────
    @FXML
    public void fnModificarVehiculo(ActionEvent event) {

        if (txtIdVehiculo.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Busque un vehículo primero.");
            return;
        }

        int idCliente = getIdClienteSeleccionado();
        if (idCliente == -1) {
            JOptionPane.showMessageDialog(null,
                    "Seleccione un cliente.");
            return;
        }

        String sql = "UPDATE [tbl.Vehiculo] SET "
                + "fk_cliente=?, marca=?, modelo=?, [año]=?, "
                + "color=?, placa=?, vin=?, observaciones=? "
                + "WHERE id_vehiculo=?";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt   (1, idCliente);
            ps.setString(2, txtMarca.getText().trim());
            ps.setString(3, txtModelo.getText().trim());

            String anoStr = txtAno.getText().trim();
            if (!anoStr.isEmpty()) {
                ps.setInt(4, Integer.parseInt(anoStr));
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            ps.setString(5, txtColor.getText().trim());
            ps.setString(6, txtPlaca.getText().trim());
            ps.setString(7, txtVIN.getText().trim());
            ps.setString(8, txtNotas.getText().trim());
            ps.setInt   (9, Integer.parseInt(txtIdVehiculo.getText().trim()));

            if (ps.executeUpdate() == 1) {
                JOptionPane.showMessageDialog(null,
                        "Vehículo modificado correctamente.");
                actualizarLista();
                fnLimpiar(null);
            } else {
                JOptionPane.showMessageDialog(null,
                        "No se encontró el vehículo para modificar.");
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "El campo AÑO debe contener solo números.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Error al modificar: " + e.getMessage());
        }
    }

    // ─── BORRAR ──────────────────────────────────────────────────
    @FXML
    public void fnBorrarVehiculo(ActionEvent event) {

        String placa = txtPlaca.getText().trim();
        if (placa.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Busque un vehículo primero.");
            return;
        }

        Alert alerta = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Seguro que desea borrar el vehículo con placa " + placa + "?",
                ButtonType.YES,
                ButtonType.NO);
        alerta.setTitle("Confirmar eliminación");

        Optional<ButtonType> res = alerta.showAndWait();

        if (res.isPresent() && res.get() == ButtonType.YES) {

            String sql = "DELETE FROM [tbl.Vehiculo] WHERE placa = ?";

            try (Connection conn = conexion.estabecerConexion();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, placa);

                if (ps.executeUpdate() == 1) {
                    JOptionPane.showMessageDialog(null,
                            "Vehículo eliminado correctamente.");
                    actualizarLista();
                    fnLimpiar(null);
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null,
                        "Error al borrar: " + e.getMessage());
            }
        }
    }

    // ─── BUSCAR ──────────────────────────────────────────────────
    @FXML
    public void fnBuscarVehiculo(ActionEvent event) {

        String idStr = txtIdVehiculo.getText().trim();

        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Ingrese un ID de vehículo para buscar.");
            return;
        }

        try {
            buscarPorId(Integer.parseInt(idStr));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "El ID debe ser un número entero.");
        }
    }

    private void buscarPorId(int id) {

        String sql = "SELECT * FROM [tbl.Vehiculo] WHERE id_vehiculo = ?";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                rellenarFormulario(rs);
            } else {
                JOptionPane.showMessageDialog(null,
                        "No se encontró ningún vehículo con ese ID.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error al buscar: " + e.getMessage());
        }
    }

    private void buscarPorPlaca(String placa) {

        String sql = "SELECT * FROM [tbl.Vehiculo] WHERE placa = ?";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, placa);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) rellenarFormulario(rs);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error al buscar: " + e.getMessage());
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
            e.printStackTrace();
        }
    }

    private void rellenarFormulario(ResultSet rs) throws SQLException {

        txtIdVehiculo.setText(rs.getString("id_vehiculo"));
        txtMarca.setText     (rs.getString("marca"));
        txtModelo.setText    (rs.getString("modelo"));
        txtAno.setText       (rs.getString("año"));
        txtColor.setText     (rs.getString("color"));
        txtPlaca.setText     (rs.getString("placa"));
        txtVIN.setText       (rs.getString("vin"));
        txtNotas.setText     (rs.getString("observaciones"));

        int idCliente = rs.getInt("fk_cliente");
        cmbCliente.getItems().stream()
                .filter(item -> item.startsWith(idCliente + " - "))
                .findFirst()
                .ifPresent(cmbCliente::setValue);
    }

    // ─── LIMPIAR ─────────────────────────────────────────────────
    @FXML
    public void fnLimpiar(ActionEvent event) {
        txtIdVehiculo.clear();
        txtMarca.clear();
        txtModelo.clear();
        txtAno.clear();
        txtColor.clear();
        txtPlaca.clear();
        txtVIN.clear();
        txtNotas.clear();
        cmbCliente.setValue(null);
        tvVehiculos.getSelectionModel().clearSelection();
    }
}