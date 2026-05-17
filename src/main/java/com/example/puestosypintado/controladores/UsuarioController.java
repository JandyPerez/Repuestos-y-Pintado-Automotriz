package com.example.puestosypintado.controladores;

import com.example.puestosypintado.Database.Conexion;
import com.example.puestosypintado.modelo.Usuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javax.swing.JOptionPane;
import java.sql.*;
import java.util.Objects;
import java.util.Optional;

import static java.lang.Integer.parseInt;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class UsuarioController {

    Conexion conexion = new Conexion();

    // --- TABLA ---
    @FXML private TableView<Usuario> tvUsuario;
    //@FXML private TableColumn<Usuario, String> colIdUsuario;
    @FXML private TableColumn<Usuario, String> colUsuario;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, String> colEstadoUsr;

    // --- FORM ---
    @FXML private TextField txtIdUsuario;
    @FXML private ComboBox cmbNombre;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtContrasena;
    @FXML private ComboBox<String> cmbRol;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private TextField txtIdEmpleado;

    public void initialize() {

        cmbRol.setItems(FXCollections.observableArrayList(
                "Admin", "Mecánico", "Recepcionista", "Cajero", "Técnico","Vendedor"
        ));

        cmbEstado.setItems(FXCollections.observableArrayList(
                "Activo", "Inactivo"
        ));

        txtIdEmpleado.textProperty().addListener((obs, oldVal, newVal) -> {
            sincronizarEmpleado();
        });

        cmbNombre.setItems(fillComboBoxEmpleado());
        cmbNombre.setValue(null);

        cmbEstado.setValue(null);

        actualizarLista();

        tvUsuario.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        buscarPorUsername(newVal.getNombre_usuario());
                    }
                });
    }

    private void sincronizarEmpleado() {

        String idText = txtIdEmpleado.getText().trim();

        if (!idText.isEmpty()) {

            try {
                int id = Integer.parseInt(idText);

                String nombre = obtenerNombreEmpleado(id);

                cmbNombre.setValue(nombre);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "ID inválido");
            }
        }
    }

    private ObservableList<String> fillComboBoxEmpleado() {

        ObservableList<String> lista = FXCollections.observableArrayList();

        String sql = "SELECT nombre_completo FROM [tbl.Empleado]";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(rs.getString("nombre_completo"));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error empleados: " + e);
        }

        return lista;
    }

    @FXML
    public void seleccionarEmpleado() {

        String nombre = cmbNombre.getValue().toString();  // 👈 importante

        if (nombre != null && !nombre.isEmpty()) {
            int id = obtenerIdEmpleado(nombre);
            if (id > 0) {
                txtIdEmpleado.setText(String.valueOf(id));
            }
        }
    }

    private String obtenerNombreEmpleado(int idEmpleado) {

        String sql = "SELECT nombre_completo FROM [tbl.Empleado] WHERE id_empleado = ?";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEmpleado);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("nombre_completo");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error empleado: " + e);
        }

        return null;
    }

    private int obtenerIdEmpleado(String nombreCompleto) {

        String sql = "SELECT id_empleado FROM [tbl.Empleado] WHERE nombre_completo = ?";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombreCompleto);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("id_empleado");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error empleado: " + e);
        }

        return -1;
    }

    // =========================
    // SELECT ALL
    // =========================
    @FXML
    protected ObservableList<Usuario> observableUsuario() {

        ObservableList<Usuario> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM [tbl.Usuario]";

        try (Connection connection = conexion.estabecerConexion();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new Usuario(
                        rs.getString("id_usuario"),
                        rs.getString("username"),
                        rs.getString("rol"),
                        rs.getString("estado")
                ));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.toString());
        }

        return lista;
    }

    public void actualizarLista() {
        //colIdUsuario.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        colUsuario.setCellValueFactory(cellData -> cellData.getValue().usuarioProperty());
        colRol.setCellValueFactory(cellData -> cellData.getValue().rolProperty());
        colEstadoUsr.setCellValueFactory(cellData -> cellData.getValue().estadoProperty());

        tvUsuario.setItems(observableUsuario());
    }

    // =========================
    // GUARDARF
    // =========================

    @FXML
    public void fnGuardarUsuario(ActionEvent event) {

        int idEmpleado = obtenerIdEmpleado(cmbNombre.getValue().toString().trim());

        String sql = "INSERT INTO [tbl.Usuario] (fk_empleado, username, password_hash, rol, estado) VALUES (?,?,?,?,?)";

        try (Connection connection = conexion.estabecerConexion();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, idEmpleado);
            pstmt.setString(2, txtUsuario.getText().trim());
            pstmt.setString(3, txtContrasena.getText().trim());
            pstmt.setString(4, cmbRol.getValue());
            pstmt.setString(5, cmbEstado.getValue());

            if (pstmt.executeUpdate() == 1) {
                JOptionPane.showMessageDialog(null, "Usuario guardado correctamente");
                actualizarLista();
                limpiar();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.toString());
        }

    }

    // =========================
    // BUSCAR
    // =========================

    @FXML
    public void fnBuscarUsuario(ActionEvent event) {
        int id = Integer.parseInt(txtIdUsuario.getText().trim());

        if (id<1) {
            JOptionPane.showMessageDialog(null, "Escriba un usuario para buscar.");
        } else{
            String sql = "SELECT * FROM [tbl.Usuario] WHERE id_usuario=" + id;
            buscarDatos(sql);
        }
    }

    private void buscarDatos(String sql) {

        try (Connection connection = conexion.estabecerConexion();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                txtIdUsuario.setText(rs.getString("id_usuario"));
                txtIdEmpleado.setText(rs.getString("fk_empleado"));
                cmbNombre.setValue(
                        obtenerNombreEmpleado(rs.getInt("fk_empleado"))
                );
                txtUsuario.setText(rs.getString("username"));
                txtContrasena.setText(rs.getString("password_hash"));

                cmbRol.getSelectionModel().select(rs.getString("rol"));
                cmbEstado.getSelectionModel().select(rs.getString("estado"));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.toString());
        }
    }

    // =========================
    // MODIFICAR
    // =========================
    @FXML
    public void fnModificarUsuario(ActionEvent event) {

        if (txtIdUsuario.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Busque un usuario primero.");
            return;
        }

        int id = parseInt(txtIdUsuario.getText().trim());

        String sql = "UPDATE [tbl.Usuario] SET " +
                "fk_empleado=" + obtenerIdEmpleado(cmbNombre.getValue().toString().trim()) + ", " +
                "username='" + txtUsuario.getText().trim() + "', " +
                "password_hash='" + txtContrasena.getText().trim() + "', " +
                "rol='" + cmbRol.getValue() + "', " +
                "estado='" + cmbEstado.getValue() + "' " +
                "WHERE id_usuario=" + id;

        ejecutarSQL(sql);
    }

    // =========================
    // BORRAR
    // =========================
    @FXML
    public void fnBorrarUsuario(ActionEvent event) {

        int id = parseInt(txtIdUsuario.getText().trim());
        String usuario = txtUsuario.getText().trim();
        if (usuario.isEmpty()) return;

        Alert alerta = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Seguro que desea borrar el usuario " + usuario + "?",
                ButtonType.YES,
                ButtonType.NO
        );

        Optional<ButtonType> res = alerta.showAndWait();

        if (res.isPresent() && res.get() == ButtonType.YES) {
            //String sql = "DELETE FROM Usuario WHERE id_usuario=" + id ;
            String sql = "update [tbl.Usuario] set estado = 'Inactivo' where id_usuario=" + id;
            ejecutarSQL(sql);
        }
    }

    // =========================
    // UTILIDAD (IGUAL CLIENTE)
    // =========================
    public void ejecutarSQL(String sql) {

        try (Connection connection = conexion.estabecerConexion();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            int result = pstmt.executeUpdate();

            if (result == 1) {
                JOptionPane.showMessageDialog(null, "Acción ejecutada correctamente");
                actualizarLista();
                limpiar();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.toString());
        }
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

    // =========================
    // LIMPIAR
    // =========================
    @FXML
    public void fnLimpiarUsuario(ActionEvent event) {
        limpiar();
    }

    public void limpiar() {
        txtIdUsuario.clear();
        txtUsuario.clear();
        txtContrasena.clear();
        txtIdEmpleado.clear();
        cmbRol.setValue(null);
        cmbEstado.setValue(null);
        cmbNombre.setValue(null);
    }

    public void buscarPorUsername(String username){
        String sql = "Select * from [tbl.Usuario] where username = ?;";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) llenarFormulario(rs);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error al buscar: " + e.getMessage());
        }
    }

    private void llenarFormulario(ResultSet rs) throws SQLException{
        txtIdUsuario.setText(rs.getString("id_usuario"));
        txtIdEmpleado.setText(rs.getString("fk_empleado"));
        cmbNombre.setValue(
                obtenerNombreEmpleado(rs.getInt("fk_empleado"))
        );
        txtUsuario.setText(rs.getString("username"));
        txtContrasena.setText(rs.getString("password_hash"));

        cmbRol.getSelectionModel().select(rs.getString("rol"));
        cmbEstado.getSelectionModel().select(rs.getString("estado"));
    }
}