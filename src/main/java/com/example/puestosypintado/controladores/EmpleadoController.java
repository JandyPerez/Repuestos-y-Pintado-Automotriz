package com.example.puestosypintado.controladores;

import com.example.puestosypintado.Database.Conexion;
import com.example.puestosypintado.modelo.Empleado;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class EmpleadoController {

    Conexion conexion = new Conexion();

    // --- ELEMENTOS FXML EMPLEADO ---
    @FXML private TableView<Empleado> tvEmpleados;
    @FXML private TableColumn<Empleado, String> colNombreEmp, colPuestoEmp, colTelEmp;
    @FXML private TextField txtIdEmp, txtNombreEmp, txtCedulaEmp, txtDireccionEmp, txtTelEmp, txtEmailEmp, txtSalarioEmp;
    @FXML private ComboBox<String> cmbPuestoEmp, cmbEstadoEmp, cmbCiudad, cmbSector;
    @FXML private DatePicker dpFechaContrato;

    @FXML
    protected ObservableList<Empleado> listaEmpleados(){
        ObservableList<Empleado> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM [tbl.Empleado]";

        try (Connection connection = conexion.estabecerConexion();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new Empleado(
                        rs.getString("nombre_completo"),
                        rs.getString("puesto"),
                        rs.getString("telefono")
                ));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al obtener empleado: " + e.toString());
        }
        return lista;
    }

    @FXML
    public void initialize() {
        dpFechaContrato.setValue(null);

        cmbPuestoEmp.setItems(FXCollections.observableArrayList("Administrador","Mecánico General", "Pintor Automotriz", "Chapista", "Detallista", "Recepcionista", "Gerente"));
        cmbEstadoEmp.setItems(FXCollections.observableArrayList("Activo", "Inactivo"));
        cmbEstadoEmp.setValue(null);

        cmbCiudad.setItems(FXCollections.observableArrayList("Santo Domingo", "Santiago de los Caballeros", "La Vega", "Puerto Plata", "San Cristóbal", "La Romana"));

        cmbCiudad.setOnAction(e -> {
            String ciudad = cmbCiudad.getValue();
            switch (ciudad) {
                case "Santo Domingo":
                    cmbSector.setItems(FXCollections.observableArrayList(
                            "Los Mina", "Villa Mella", "Naco", "Gascue", "Bella Vista"
                    ));
                    break;

                case "Santiago de los Caballeros":
                    cmbSector.setItems(FXCollections.observableArrayList(
                            "Los Jardines", "Gurabo", "Cienfuegos", "Pekín", "Bella Vista"
                    ));
                    break;

                case "La Vega":
                    cmbSector.setItems(FXCollections.observableArrayList(
                            "Centro", "Villa Rosa", "Cutupú", "Burende", "Los Pomos"
                    ));
                    break;

                case "Puerto Plata":
                    cmbSector.setItems(FXCollections.observableArrayList(
                            "Centro", "Ensanche Dubocq", "Cerro Alto", "Padre Granero", "Cofresí"
                    ));
                    break;

                case "San Cristóbal":
                    cmbSector.setItems(FXCollections.observableArrayList(
                            "Madre Vieja", "Canastica", "Haina", "Yaguate", "Najayo"
                    ));
                    break;

                case "La Romana":
                    cmbSector.setItems(FXCollections.observableArrayList(
                            "Villa Verde", "Caleta", "Savica", "Quisqueya", "Villa Hermosa"
                    ));
                    break;

            }});

        colNombreEmp.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
        colPuestoEmp.setCellValueFactory(cellData -> cellData.getValue().puestoProperty());
        colTelEmp.setCellValueFactory(cellData -> cellData.getValue().telefonoProperty());

        tvEmpleados.setItems(listaEmpleados());

        tvEmpleados.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        buscarPorNombre(newVal.getNombre());
                    }
                });
    }

    @FXML
    protected void fnGuardarEmpleado(ActionEvent event) {

        String nombre = txtNombreEmp.getText().trim();
        String cedula = txtCedulaEmp.getText().trim();
        String telefono = txtTelEmp.getText().trim();
        String email = txtEmailEmp.getText().trim();
        String direccion = txtDireccionEmp.getText().trim();
        String ciudad = cmbCiudad.getValue();
        String sector = cmbSector.getValue();
        String puesto = cmbPuestoEmp.getValue();
        String salario = txtSalarioEmp.getText().trim();
        LocalDate fecha = dpFechaContrato.getValue();
        String estado = cmbEstadoEmp.getValue();

        if (nombre.isEmpty() || cedula.isEmpty() || telefono.isEmpty() || puesto == null) {
            JOptionPane.showMessageDialog(null, "Nombre, Cédula, Teléfono y Puesto son obligatorios.");
            return;
        } else if (existeCedula(cedula)) {
            JOptionPane.showMessageDialog(null, "LA CÉDULA YA ESTA GUARDADA EN LA BD!!");
        }

        String sql = "INSERT INTO [tbl.Empleado] " +
                "(nombre_completo, cedula_rnc, telefono, email, direccion, ciudad, sector, puesto, salario, fecha_contratacion, estado) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?)";


        try (Connection connection = conexion.estabecerConexion();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, nombre);
            pstmt.setString(2, cedula);
            pstmt.setString(3, telefono);
            pstmt.setString(4, email);
            pstmt.setString(5, direccion);
            pstmt.setString(6, ciudad);
            pstmt.setString(7, sector);
            pstmt.setString(8, puesto);
            pstmt.setString(9, salario);

            LocalDate hoy = LocalDate.now();
            if (fecha.isAfter(hoy)) {
                JOptionPane.showMessageDialog(null,"No se pueden ingresar fechas mayores que hoy");
            } else if (fecha != null) {
                pstmt.setDate(10, java.sql.Date.valueOf(fecha));
            } else {
                pstmt.setDate(10, null);
            }

            pstmt.setString(11, estado);

            if (pstmt.executeUpdate() == 1) {
                JOptionPane.showMessageDialog(null, "Empleado guardado correctamente");
                actualizarLista();
                fnLimpiarEmp(null);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al guardar: " + e.toString());
        }
    }

    @FXML
    public void fnModificarEmpleado(ActionEvent event) {

        if (txtIdEmp.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Busque un empleado primero para editarlo.");
            return;
        }

        int id = Integer.parseInt(txtIdEmp.getText().trim());

        String sql = "UPDATE [tbl.Empleado] SET " +
                "nombre_completo=?, cedula_rnc=?, telefono=?, email=?, direccion=?, ciudad=?, sector=?," +
                "puesto=?, salario=?, fecha_contratacion=?, estado=? " +
                "WHERE id_empleado=?;";

        try (Connection connection = conexion.estabecerConexion();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, txtNombreEmp.getText().trim());
            pstmt.setString(2, txtCedulaEmp.getText().trim());
            pstmt.setString(3, txtTelEmp.getText().trim());
            pstmt.setString(4, txtEmailEmp.getText().trim());
            pstmt.setString(5, txtDireccionEmp.getText().trim());
            pstmt.setString(6, cmbCiudad.getValue());
            pstmt.setString(7, cmbSector.getValue());
            pstmt.setString(8, cmbPuestoEmp.getValue());
            pstmt.setString(9, txtSalarioEmp.getText().trim());

            LocalDate hoy = LocalDate.now();
            if (dpFechaContrato.getValue().isAfter(hoy)) {
                JOptionPane.showMessageDialog(null,"No se pueden ingresar fechas mayores que hoy");
            } else if (dpFechaContrato.getValue() != null) {
                pstmt.setDate(10, java.sql.Date.valueOf(dpFechaContrato.getValue()));
            } else {
                pstmt.setDate(10, null);
            }

            pstmt.setString(11, cmbEstadoEmp.getValue());

            pstmt.setInt(12, id);

            if (pstmt.executeUpdate() == 1) {
                JOptionPane.showMessageDialog(null, "Empleado actualizado correctamente");

                actualizarLista();
                fnLimpiarEmp(null);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar: " + e.toString());
        }
    }

    @FXML
    public void fnBorrarEmpleado(ActionEvent event) {
        int id = Integer.parseInt(txtIdEmp.getText().trim());
        String nombre = txtNombreEmp.getText().trim();

        if (nombre.isEmpty()) System.out.println("No se puede borrar a la nada, rey");

        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Seguro que desea borrar a " + nombre + "?",
                ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> resultado = alerta.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.YES) {

            //String sql = "DELETE FROM Empleado WHERE id_empleado=?";
            String sql = "Update [tbl.Empleado] set estado = 'Inactivo' where id_empleado=?";

            try (Connection connection = conexion.estabecerConexion();
                 PreparedStatement pstmt = connection.prepareStatement(sql)) {

                pstmt.setInt(1, id);

                if (pstmt.executeUpdate() == 1) {
                    JOptionPane.showMessageDialog(null, "Empleado \"eliminado\" (desactivado :p) correctamente ");

                    actualizarLista(); // refresca tabla
                    fnLimpiarEmp(null);
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error al eliminar: " + e.toString());
            }
        }
    }

    @FXML
    public void fnBuscarEmpleado(ActionEvent event) {
        if (!txtIdEmp.getText().trim().isEmpty()){
            String id = txtIdEmp.getText().trim();
            String sql = "SELECT * FROM [tbl.Empleado] WHERE id_empleado=" + id;
            buscarDatosEmpleado(sql);
        } else if (!txtCedulaEmp.getText().trim().isEmpty()) {
            String cedula = txtCedulaEmp.getText().trim();
            String sql = "SELECT * FROM [tbl.Empleado] WHERE cedula_rnc='" + cedula + "'";
            buscarDatosEmpleado(sql);
        } else if(!txtNombreEmp.getText().trim().isEmpty()) {
            String nombre = txtNombreEmp.getText().trim();
            String sql = "SELECT * FROM [tbl.Empleado] WHERE nombre_completo='" + nombre + "'";
            buscarDatosEmpleado(sql);
        } else {
            JOptionPane.showMessageDialog(null,"Y no puedo imaginarme quien quieres buscar. Pon el ID, cédula o nombre completo");
        }
    }

    private void buscarDatosEmpleado(String sql) {

        try (Connection connection = conexion.estabecerConexion();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {

                txtIdEmp.setText(resultSet.getString("id_empleado"));
                txtNombreEmp.setText(resultSet.getString("nombre_completo"));
                txtCedulaEmp.setText(resultSet.getString("cedula_rnc"));
                txtTelEmp.setText(resultSet.getString("telefono"));
                txtEmailEmp.setText(resultSet.getString("email"));
                txtDireccionEmp.setText(resultSet.getString("direccion"));
                cmbCiudad.setValue(resultSet.getString("ciudad").trim());
                cmbSector.setValue(resultSet.getString("sector").trim());
                cmbPuestoEmp.setValue(resultSet.getString("puesto").trim());
                txtSalarioEmp.setText(resultSet.getString("salario"));
                dpFechaContrato.setValue(
                        resultSet.getDate("fecha_contratacion").toLocalDate()
                );
                cmbEstadoEmp.setValue(resultSet.getString("estado").trim());

            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.toString());
        }
    }

    @FXML
    public void fnLimpiarEmp(ActionEvent event) {
        txtIdEmp.clear(); txtNombreEmp.clear(); txtCedulaEmp.clear(); txtDireccionEmp.clear();
        txtTelEmp.clear(); txtEmailEmp.clear(); txtSalarioEmp.clear();
        cmbPuestoEmp.setValue(null); dpFechaContrato.setValue(null);
        cmbEstadoEmp.setValue(null);
        cmbCiudad.setValue(null); cmbSector.setValue(null);
    }

    private boolean existeCedula(String cedula) {
        String sql = "SELECT COUNT(*) FROM [tbl.Empleado] WHERE cedula_rnc = ?";
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

    private boolean validarFormEmp() {
        if (txtNombreEmp.getText().isEmpty() || txtCedulaEmp.getText().isEmpty() ||
                txtTelEmp.getText().isEmpty() || cmbPuestoEmp.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Llene los campos obligatorios (*).");
            return false;
        }
        return true;
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

    public void actualizarLista(){
        colNombreEmp.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
        colPuestoEmp.setCellValueFactory(cellData -> cellData.getValue().puestoProperty());
        colTelEmp.setCellValueFactory(cellData -> cellData.getValue().telefonoProperty());

        tvEmpleados.setItems(listaEmpleados());
    }

    public void buscarPorNombre(String nombre){
        String sql = "Select * from [tbl.Empleado] where nombre_completo = ?;";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) llenarFormulario(rs);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error al buscar: " + e.getMessage());
        }
    }

    private void llenarFormulario(ResultSet resultSet) throws SQLException{
        txtIdEmp.setText(resultSet.getString("id_empleado"));
        txtNombreEmp.setText(resultSet.getString("nombre_completo"));
        txtCedulaEmp.setText(resultSet.getString("cedula_rnc"));
        txtTelEmp.setText(resultSet.getString("telefono"));
        txtEmailEmp.setText(resultSet.getString("email"));
        txtDireccionEmp.setText(resultSet.getString("direccion"));
        cmbCiudad.setValue(resultSet.getString("ciudad").trim());
        cmbSector.setValue(resultSet.getString("sector").trim());
        cmbPuestoEmp.setValue(resultSet.getString("puesto").trim());
        txtSalarioEmp.setText(resultSet.getString("salario"));
        dpFechaContrato.setValue(
                resultSet.getDate("fecha_contratacion").toLocalDate()
        );
        cmbEstadoEmp.setValue(resultSet.getString("estado").trim());

    }
}