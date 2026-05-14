package com.example.puestosypintado.controladores;

// Importa tus clases de conexión y modelos (Ajusta los paquetes si es necesario)
// import com.example.puestosypintado.Database.Conexion;
import com.example.puestosypintado.modelo.OrdenTrabajo;
import com.example.puestosypintado.modelo.SubservicioPintura;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javax.swing.JOptionPane;
import java.sql.*;

public class PinturaController {


    // --- ELEMENTOS FXML GENERALES ---
    @FXML private TableView<OrdenTrabajo> tvOrdenes;
    @FXML private TableColumn<OrdenTrabajo, Integer> colOT;
    @FXML private TableColumn<OrdenTrabajo, String> colVehiculo, colEstado;

    @FXML private TextField txtIdOT, txtPiezas, txtPresupuesto, txtAnticipo;
    @FXML private ComboBox<String> cmbVehiculo;

    // --- ELEMENTOS FXML SUBSERVICIOS ---
    @FXML private ComboBox<String> cmbPiezaSeleccionada;
    @FXML private TextField txtCostoPiezaIndividual;
    @FXML private CheckBox chkLimpieza, chkLijado, chkImprimacion, chkBarnizado;

    @FXML private TableView<SubservicioPintura> tvSubservicios;
    @FXML private TableColumn<SubservicioPintura, String> colDetallePieza, colServicios;
    @FXML private TableColumn<SubservicioPintura, Double> colCostoSub;
    @FXML private Label lblTotalSubservicios;

    // Lista observable para la tablita de subservicios
    private ObservableList<SubservicioPintura> listaSubservicios = FXCollections.observableArrayList();
    private double totalAcumuladoSubservicios = 0.0;

    @FXML
    public void initialize() {
        // 1. Configurar columnas de la tabla de subservicios
        colDetallePieza.setCellValueFactory(cellData -> cellData.getValue().piezaProperty());
        colServicios.setCellValueFactory(cellData -> cellData.getValue().serviciosProperty());
        colCostoSub.setCellValueFactory(cellData -> cellData.getValue().costoTotalProperty().asObject());

        tvSubservicios.setItems(listaSubservicios);

        // 2. MAGIA: Listener para que el txtPiezas llene el cmbPiezaSeleccionada automáticamente
        txtPiezas.textProperty().addListener((observable, oldValue, newValue) -> {
            cmbPiezaSeleccionada.getItems().clear();
            if (!newValue.trim().isEmpty()) {
                String[] piezasArray = newValue.split(",");
                for (String pieza : piezasArray) {
                    if (!pieza.trim().isEmpty()) {
                        cmbPiezaSeleccionada.getItems().add(pieza.trim());
                    }
                }
            }
        });
    }

    @FXML
    public void fnAgregarSubservicio(ActionEvent event) {
        if (cmbPiezaSeleccionada.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Por favor, seleccione una pieza del ComboBox.");
            return;
        }

        try {
            double costoBase = Double.parseDouble(txtCostoPiezaIndividual.getText().trim());
            double costoSubtotal = costoBase;
            StringBuilder serviciosAplicados = new StringBuilder();

            // Sumamos 150 por cada check seleccionado
            if (chkLimpieza.isSelected()) { costoSubtotal += 150; serviciosAplicados.append("Limpieza, "); }
            if (chkLijado.isSelected()) { costoSubtotal += 150; serviciosAplicados.append("Lijado, "); }
            if (chkImprimacion.isSelected()) { costoSubtotal += 150; serviciosAplicados.append("Imprimación, "); }
            if (chkBarnizado.isSelected()) { costoSubtotal += 150; serviciosAplicados.append("Barnizado, "); }

            String serviciosFinal = serviciosAplicados.toString();
            // Quitamos la última coma y espacio si hay servicios
            if (!serviciosFinal.isEmpty()) {
                serviciosFinal = serviciosFinal.substring(0, serviciosFinal.length() - 2);
            } else {
                serviciosFinal = "Solo pieza base";
            }

            // Agregamos a la tabla
            SubservicioPintura nuevoDetalle = new SubservicioPintura(cmbPiezaSeleccionada.getValue(), serviciosFinal, costoSubtotal);
            listaSubservicios.add(nuevoDetalle);

            // Actualizamos el Label del Total
            totalAcumuladoSubservicios += costoSubtotal;
            lblTotalSubservicios.setText("$ " + String.format("%.2f", totalAcumuladoSubservicios));

            // Limpiamos los checks para la siguiente pieza
            chkLimpieza.setSelected(false);
            chkLijado.setSelected(false);
            chkImprimacion.setSelected(false);
            chkBarnizado.setSelected(false);
            cmbPiezaSeleccionada.setValue(null);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "El costo base de la pieza debe ser un número válido.");
        }
    }

    @FXML
    public void fnGuardarOT(ActionEvent event) {
        if (listaSubservicios.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Debe añadir al menos un detalle de pieza antes de guardar.");
            return;
        }

        // --- LÓGICA DE GUARDADO EN BD ---
        /*
        try (Connection conn = conexion.estabecerConexion()) {
            conn.setAutoCommit(false); // Transacción para asegurar que todo se guarde bien

            // 1. Insertar la OT principal aquí y obtener su ID generado (idOTGenerado)
            // ... (Tu código de INSERT INTO OrdenTrabajo)
            int idOTGenerado = 1; // Suponiendo que este es el ID que te devolvió la BD

            // 2. Insertar todos los subservicios de la tablita recorriendo la lista
            String sqlDetalle = "INSERT INTO Detalle_Subservicios_Pintura (id_ot, pieza, servicios_aplicados, costo_total) VALUES (?,?,?,?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlDetalle)) {
                for (SubservicioPintura sub : listaSubservicios) {
                    pstmt.setInt(1, idOTGenerado);
                    pstmt.setString(2, sub.getPieza());
                    pstmt.setString(3, sub.getServicios());
                    pstmt.setDouble(4, sub.getCostoTotal());
                    pstmt.addBatch(); // Agregamos al lote
                }
                pstmt.executeBatch(); // Ejecutamos todo el lote de inserciones
            }

            conn.commit();
            JOptionPane.showMessageDialog(null, "Orden y detalles guardados correctamente.");
            fnLimpiar(null);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al guardar: " + e.getMessage());
        }
        */
        JOptionPane.showMessageDialog(null, "Simulación: Guardado exitoso de la OT y " + listaSubservicios.size() + " piezas.");
    }

    @FXML
    public void fnBuscarOT(ActionEvent event) {
        // Lógica de búsqueda
    }

    @FXML
    public void fnModificarOT(ActionEvent event) {
        // Lógica de actualización
    }

    @FXML
    public void fnLimpiar(ActionEvent event) {
        txtIdOT.clear();
        cmbVehiculo.setValue(null);
        txtPiezas.clear();
        txtPresupuesto.clear();
        txtAnticipo.clear();
        txtCostoPiezaIndividual.setText("150");

        chkLimpieza.setSelected(false);
        chkLijado.setSelected(false);
        chkImprimacion.setSelected(false);
        chkBarnizado.setSelected(false);

        listaSubservicios.clear();
        totalAcumuladoSubservicios = 0.0;
        lblTotalSubservicios.setText("$ 0.00");
    }
}
