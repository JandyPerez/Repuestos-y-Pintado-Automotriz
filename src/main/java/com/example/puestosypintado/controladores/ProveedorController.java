package com.example.puestosypintado.controladores;

import com.example.puestosypintado.Database.Conexion;
import com.example.puestosypintado.modelo.Proveedor;
import javafx.beans.property.SimpleStringProperty;
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

public class ProveedorController {

    Conexion conexion = new Conexion();

    // ─── FXML ────────────────────────────────────────────────────
    @FXML private TextField        txtIdProv;
    @FXML private TextField        txtRncProv;
    @FXML private TextField        txtEmpresaProv;
    @FXML private TextField        txtDireccionProv;
    @FXML private TextField        txtTelProv;
    @FXML private TextField        txtEmailProv;
    @FXML private TextField        txtContactoProv;
    @FXML private TextField        txtTelContactoProv;
    @FXML private TextArea         taNotasProv;

    // FIX #16/#19: cmbCategoriaProv y cmbCiudad ahora existen en el FXML
    @FXML private ComboBox<String> cmbCategoriaProv;
    @FXML private ComboBox<String> cmbEstadoProv;
    @FXML private ComboBox<String> cmbSector;
    @FXML private ComboBox<String> cmbCiudad;         // FIX #19: antes era cmbSector1

    @FXML private TableView<Proveedor>            tvProveedores;
    @FXML private TableColumn<Proveedor, String>  colIdProv;
    @FXML private TableColumn<Proveedor, String>  colEmpresaProv;
    @FXML private TableColumn<Proveedor, String>  colContactoProv;
    // FIX #15: colTelProv eliminado (no existe en el FXML)
    @FXML private TableColumn<Proveedor, String>  colCategoriaProv;

    // ─── INITIALIZE ──────────────────────────────────────────────
    public void initialize() {

        // Categorías fijas
        cmbCategoriaProv.setItems(FXCollections.observableArrayList(
                "Pintura", "Repuestos", "Consumibles", "Herramientas", "Servicios Externos"
        ));

        // Estado
        cmbEstadoProv.setItems(FXCollections.observableArrayList(
                "ACTIVO", "INACTIVO"
        ));
        cmbEstadoProv.setValue("ACTIVO");

        // Sectores y ciudades (ajusta según tu catálogo)
        cmbSector.setItems(FXCollections.observableArrayList(
                "Norte", "Sur", "Este", "Oeste", "Centro"
        ));
        cmbCiudad.setItems(FXCollections.observableArrayList(          // FIX #19
                "Santo Domingo", "Santiago", "San Pedro", "La Vega",
                "San Francisco de Macoris", "Otra"
        ));

        // TableView columns
        colIdProv.setCellValueFactory(cell ->
                new SimpleStringProperty(String.valueOf(cell.getValue().getIdProveedor())));
        colEmpresaProv.setCellValueFactory (cell -> cell.getValue().empresaProperty());
        colContactoProv.setCellValueFactory(cell -> cell.getValue().contactoProperty());
        colCategoriaProv.setCellValueFactory(cell -> cell.getValue().categoriaProperty());

        tvProveedores.setItems(observableProveedor());

        // FIX #17: Clic en fila → rellena el formulario
        tvProveedores.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        buscarPorId(newVal.getIdProveedor());
                    }
                });
    }

    // ─── SELECT ALL → TableView ───────────────────────────────────
    // FIX #1/#2/#3: columnas corregidas a los nombres reales de la BD
    protected ObservableList<Proveedor> observableProveedor() {

        ObservableList<Proveedor> lista = FXCollections.observableArrayList();

        String sql = "SELECT id_proveedor, nombre_empresa, nombre_contacto, categoria "
                + "FROM Proveedor";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Proveedor(
                        rs.getString("id_proveedor"),
                        rs.getString("nombre_empresa"),   // FIX #1
                        rs.getString("nombre_contacto"),  // FIX #2
                        rs.getString("categoria")
                ));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al cargar lista: " + e.getMessage());
        }

        return lista;
    }

    public void actualizarLista() {
        tvProveedores.setItems(observableProveedor());
    }


    private boolean existeRNC(String cedula) {
        String sql = "SELECT COUNT(*) FROM Proveedor WHERE rnc_nit = ?";
        try (Connection connection = conexion.estabecerConexion();
             PreparedStatement pstmt = connection.prepareStatement(sql)){

            pstmt.setString(1, cedula);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0; // true si existe
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar: " + e.toString());
        }
        return false;
    }

    // ─── GUARDAR ─────────────────────────────────────────────────
    // FIX #4–#9: nombres de columna corregidos a los reales de la BD
    @FXML
    public void fnGuardarProveedor(ActionEvent event) {

        String rnc     = txtRncProv.getText().trim();
        String empresa = txtEmpresaProv.getText().trim();
        String estado  = cmbEstadoProv.getValue();

        if (rnc.isEmpty() || empresa.isEmpty()) {
            JOptionPane.showMessageDialog(null, "RNC/NIT y Nombre de Empresa son obligatorios.");
            return;
        } else if (estado == null) {
            estado = "Activo";
        } else if (existeRNC(rnc)) {
            JOptionPane.showMessageDialog(null, "EL RNC YA ESTA REGISTRADO");
            return;
        }
        String sql = "INSERT INTO Proveedor "
                + "(rnc_nit, nombre_empresa, direccion, telefono_empresa, correo_empresa, "
                + " nombre_contacto, telefono_contacto, categoria, notas, estado, sector, ciudad) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1,  rnc);
            ps.setString(2,  empresa);
            ps.setString(3,  txtDireccionProv.getText().trim());
            ps.setString(4,  txtTelProv.getText().trim());         // FIX #6: telefono_empresa
            ps.setString(5,  txtEmailProv.getText().trim());       // FIX #7: correo_empresa
            ps.setString(6,  txtContactoProv.getText().trim());    // FIX #8: nombre_contacto
            ps.setString(7,  txtTelContactoProv.getText().trim()); // FIX #9: telefono_contacto
            ps.setString(8,  cmbCategoriaProv.getValue());
            ps.setString(9,  taNotasProv.getText().trim());
            ps.setString(10, estado);
            ps.setString(11, cmbSector.getValue());
            ps.setString(12, cmbCiudad.getValue());               // FIX #19

            if (ps.executeUpdate() == 1) {
                JOptionPane.showMessageDialog(null, "Proveedor guardado correctamente.");
                actualizarLista();
                fnLimpiarProv(null);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al guardar: " + e.getMessage());
        }
    }

    // ─── MODIFICAR ───────────────────────────────────────────────
    // FIX #4–#9: nombres de columna corregidos
    @FXML
    public void fnModificarProveedor(ActionEvent event) {

        if (txtIdProv.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Busque un proveedor primero.");
            return;
        }

        String sql = "UPDATE Proveedor SET "
                + "rnc_nit=?, nombre_empresa=?, direccion=?, telefono_empresa=?, "
                + "correo_empresa=?, nombre_contacto=?, telefono_contacto=?, "
                + "categoria=?, notas=?, estado=?, sector=?, ciudad=? "
                + "WHERE id_proveedor=?";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1,  txtRncProv.getText().trim());
            ps.setString(2,  txtEmpresaProv.getText().trim());
            ps.setString(3,  txtDireccionProv.getText().trim());
            ps.setString(4,  txtTelProv.getText().trim());
            ps.setString(5,  txtEmailProv.getText().trim());
            ps.setString(6,  txtContactoProv.getText().trim());
            ps.setString(7,  txtTelContactoProv.getText().trim());
            ps.setString(8,  cmbCategoriaProv.getValue());
            ps.setString(9,  taNotasProv.getText().trim());
            ps.setString(10, cmbEstadoProv.getValue());
            ps.setString(11, cmbSector.getValue());
            ps.setString(12, cmbCiudad.getValue());
            ps.setInt   (13, Integer.parseInt(txtIdProv.getText().trim()));

            if (ps.executeUpdate() == 1) {
                JOptionPane.showMessageDialog(null, "Proveedor actualizado correctamente.");
                actualizarLista();
                fnLimpiarProv(null);
            } else {
                JOptionPane.showMessageDialog(null, "No se encontró el proveedor para modificar.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al modificar: " + e.getMessage());
        }
    }

    // ─── BORRAR ──────────────────────────────────────────────────
    // FIX #11: PreparedStatement con parámetro ? (sin concatenación)
    @FXML
    public void fnBorrarProveedor(ActionEvent event) {

        if (txtIdProv.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Busque un proveedor primero.");
            return;
        }

        String empresa = txtEmpresaProv.getText().trim();

        Alert alerta = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Seguro que desea eliminar el proveedor: " + empresa + "?",
                ButtonType.YES,
                ButtonType.NO);
        alerta.setTitle("Confirmar eliminación");

        Optional<ButtonType> result = alerta.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {

            String sql = "DELETE FROM Proveedor WHERE id_proveedor = ?"; // FIX #11

            try (Connection conn = conexion.estabecerConexion();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, Integer.parseInt(txtIdProv.getText().trim()));

                if (ps.executeUpdate() == 1) {
                    JOptionPane.showMessageDialog(null, "Proveedor eliminado correctamente.");
                    actualizarLista();
                    fnLimpiarProv(null);
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error al borrar: " + e.getMessage());
            }
        }
    }

    // ─── BUSCAR (botón ? junto a txtIdProv) ──────────────────────
    // FIX #12/#14: busca por id_proveedor con PreparedStatement
    @FXML
    public void fnBuscarProveedor(ActionEvent event) {

        String idStr = txtIdProv.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Ingrese un ID de proveedor para buscar.");
            return;
        }

        try {
            buscarPorId(Integer.parseInt(idStr));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "El ID debe ser un número entero.");
        }
    }

    // Busca por id_proveedor y rellena el formulario completo
    // FIX #1–#9/#12/#13: columnas correctas + PreparedStatement con parámetro
    private void buscarPorId(int id) {

        String sql = "SELECT * FROM Proveedor WHERE id_proveedor = ?"; // FIX #12/#13

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                rellenarFormulario(rs);
            } else {
                JOptionPane.showMessageDialog(null,
                        "No se encontró ningún proveedor con ese ID.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al buscar: " + e.getMessage());
        }
    }

    // Rellena todos los campos del formulario desde un ResultSet
    // FIX #1–#9: nombres de columna reales de la BD
    private void rellenarFormulario(ResultSet rs) throws SQLException {

        txtIdProv.setText          (rs.getString("id_proveedor"));
        txtRncProv.setText         (rs.getString("rnc_nit"));             // FIX #4
        txtEmpresaProv.setText     (rs.getString("nombre_empresa"));      // FIX #1/#5
        txtDireccionProv.setText   (rs.getString("direccion"));
        txtTelProv.setText         (rs.getString("telefono_empresa"));    // FIX #3/#6
        txtEmailProv.setText       (rs.getString("correo_empresa"));      // FIX #7
        txtContactoProv.setText    (rs.getString("nombre_contacto"));     // FIX #2/#8
        txtTelContactoProv.setText (rs.getString("telefono_contacto"));   // FIX #9
        taNotasProv.setText        (rs.getString("notas"));

        cmbCategoriaProv.setValue  (rs.getString("categoria"));
        cmbEstadoProv.setValue     (rs.getString("estado"));
        cmbSector.setValue         (rs.getString("sector"));
        cmbCiudad.setValue         (rs.getString("ciudad"));              // FIX #19
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

    // ─── LIMPIAR ─────────────────────────────────────────────────
    @FXML
    public void fnLimpiarProv(ActionEvent event) {

        txtIdProv.clear();
        txtRncProv.clear();
        txtEmpresaProv.clear();
        txtDireccionProv.clear();
        txtTelProv.clear();
        txtEmailProv.clear();
        txtContactoProv.clear();
        txtTelContactoProv.clear();
        taNotasProv.clear();

        cmbCategoriaProv.setValue(null);
        cmbEstadoProv.setValue("ACTIVO");
        cmbSector.setValue(null);
        cmbCiudad.setValue(null);

        tvProveedores.getSelectionModel().clearSelection();
    }
}