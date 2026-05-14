package com.example.puestosypintado.controladores;

import com.example.puestosypintado.Database.Conexion;
import com.example.puestosypintado.modelo.Incidencia;
import com.example.puestosypintado.util.Sesion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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

/**
 * Controlador: Pantalla 10 — Incidencias Abiertas (Panel de supervisión)
 * READ  → vw_IncidenciasAbiertas
 *         (filtro por tipo, estado y búsqueda libre por cliente/placa)
 * Desde aquí:
 *   · Abre pantalla 7 (Resolver Incidencia) con la incidencia seleccionada
 *   · Abre pantalla 4 (Detalle Servicio) con el servicio asociado
 */
public class IncidenciasAbiertasController {

    Conexion conexion = new Conexion();

    // ─── FXML — Filtros ───────────────────────────────────────────
    @FXML private ComboBox<String> cmbFiltroTipo;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private TextField        txtBuscar;

    // ─── FXML — Tabla principal ───────────────────────────────────
    @FXML private TableView<Incidencia>           tvIncidencias;
    @FXML private TableColumn<Incidencia, String> colId;
    @FXML private TableColumn<Incidencia, String> colOrden;
    @FXML private TableColumn<Incidencia, String> colCliente;
    @FXML private TableColumn<Incidencia, String> colPlaca;
    @FXML private TableColumn<Incidencia, String> colSubservicio;
    @FXML private TableColumn<Incidencia, String> colTipoDefecto;
    @FXML private TableColumn<Incidencia, String> colEstado;
    @FXML private TableColumn<Incidencia, String> colFecha;
    @FXML private TableColumn<Incidencia, String> colSupervisor;

    // ─── FXML — Panel de detalle ──────────────────────────────────
    @FXML private TextField txtDescripcionDetalle;
    @FXML private TextField txtTrabajoDetalle;
    @FXML private Label     lblContadorIncidencias;
    @FXML private Button    btnResolver;
    @FXML private Button    btnVerServicio;

    // Lista completa (sin filtros) para FilteredList
    private final ObservableList<Incidencia> listaCompleta =
            FXCollections.observableArrayList();

    // ─── INITIALIZE ──────────────────────────────────────────────
    public void initialize() {

        colId.setCellValueFactory          (c -> c.getValue().idIncidenciaProperty());
        colOrden.setCellValueFactory       (c -> c.getValue().idServicioProperty());
        colCliente.setCellValueFactory     (c -> c.getValue().clienteProperty());
        colPlaca.setCellValueFactory       (c -> c.getValue().placaProperty());
        colSubservicio.setCellValueFactory (c -> c.getValue().subservicioAfectadoProperty());
        colTipoDefecto.setCellValueFactory (c -> c.getValue().tipoDefectoProperty());
        colEstado.setCellValueFactory      (c -> c.getValue().estadoProperty());
        colFecha.setCellValueFactory       (c -> c.getValue().fechaDeteccionProperty());
        colSupervisor.setCellValueFactory  (c -> c.getValue().supervisorProperty());

        // Filtros de tipo
        cmbFiltroTipo.setItems(FXCollections.observableArrayList(
                "Todos",
                "color_incorrecto",
                "dano_carroceria",
                "masillado_defectuoso",
                "pintura_defectuosa",
                "superficie_no_debida",
                "barniz_defectuoso",
                "otro"
        ));
        cmbFiltroTipo.setValue("Todos");

        // Filtros de estado
        cmbFiltroEstado.setItems(FXCollections.observableArrayList(
                "Todos", "abierta", "en_correccion"
        ));
        cmbFiltroEstado.setValue("Todos");

        // Al seleccionar fila → rellenar panel de detalle y habilitar botones
        tvIncidencias.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) rellenarDetalle(newVal);
                });

        // Aplicar filtros al cambiar combos o texto
        cmbFiltroTipo.valueProperty() .addListener((obs, o, n) -> aplicarFiltros());
        cmbFiltroEstado.valueProperty().addListener((obs, o, n) -> aplicarFiltros());
        txtBuscar.textProperty()      .addListener((obs, o, n) -> aplicarFiltros());

        // Botones deshabilitados hasta seleccionar una fila
        btnResolver.setDisable    (true);
        btnVerServicio.setDisable (true);

        cargarIncidencias();
    }

    // ─── CARGAR vw_IncidenciasAbiertas ────────────────────────────
    private void cargarIncidencias() {
        listaCompleta.clear();

        // vw_IncidenciasAbiertas ya filtra estado IN ('abierta','en_correccion')
        String sql =
                "SELECT id_incidencia, id_servicioPintura, cliente, placa, "
                        + "       ISNULL(subservicio_afectado, '--') AS subservicio_afectado, "
                        + "       tipo_defecto, descripcion, "
                        + "       CONVERT(VARCHAR, fecha_deteccion, 103) AS fecha_det, "
                        + "       supervisor, estado "
                        + "FROM vw_IncidenciasAbiertas "
                        + "ORDER BY fecha_deteccion DESC";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                listaCompleta.add(new Incidencia(
                        rs.getString("id_incidencia"),
                        rs.getString("id_servicioPintura"),
                        rs.getString("cliente"),
                        rs.getString("placa"),
                        rs.getString("subservicio_afectado"),
                        rs.getString("tipo_defecto"),
                        rs.getString("descripcion"),
                        rs.getString("fecha_det"),
                        rs.getString("supervisor"),
                        rs.getString("estado")
                ));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error cargando incidencias: " + e.getMessage());
        }

        aplicarFiltros();
    }

    // ─── FILTROS ─────────────────────────────────────────────────
    private void aplicarFiltros() {

        String filtroTipo   = cmbFiltroTipo.getValue();
        String filtroEstado = cmbFiltroEstado.getValue();
        String busqueda     = txtBuscar.getText().trim().toLowerCase();

        FilteredList<Incidencia> filtrada = new FilteredList<>(listaCompleta, inc -> {

            boolean okTipo = "Todos".equals(filtroTipo)
                    || inc.getTipoDefecto().equals(filtroTipo);

            boolean okEstado = "Todos".equals(filtroEstado)
                    || inc.getEstado().equals(filtroEstado);

            boolean okBusqueda = busqueda.isEmpty()
                    || inc.getCliente().toLowerCase().contains(busqueda)
                    || inc.getPlaca().toLowerCase().contains(busqueda);

            return okTipo && okEstado && okBusqueda;
        });

        tvIncidencias.setItems(filtrada);
        lblContadorIncidencias.setText(String.valueOf(filtrada.size()));

        // Limpiar detalle al cambiar filtros
        txtDescripcionDetalle.clear();
        txtTrabajoDetalle.clear();
        btnResolver.setDisable   (true);
        btnVerServicio.setDisable(true);
    }

    // ─── RELLENAR PANEL DE DETALLE ────────────────────────────────
    private void rellenarDetalle(Incidencia inc) {
        txtDescripcionDetalle.setText(inc.getDescripcion());

        // Cargar descripcion_trabajo del servicio asociado
        String sql =
                "SELECT descripcion_trabajo "
                        + "FROM [tbl.Servicio_Pintura] "
                        + "WHERE id_servicioPintura = ?";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(inc.getIdServicio()));
            ResultSet rs = ps.executeQuery();
            txtTrabajoDetalle.setText(
                    rs.next() ? rs.getString("descripcion_trabajo") : "--");

        } catch (Exception e) {
            txtTrabajoDetalle.setText("--");
        }

        btnResolver.setDisable   (false);
        btnVerServicio.setDisable(false);
    }

    // ─── REFRESCAR ───────────────────────────────────────────────
    @FXML
    public void fnRefrescar(ActionEvent event) {
        tvIncidencias.getSelectionModel().clearSelection();
        txtDescripcionDetalle.clear();
        txtTrabajoDetalle.clear();
        btnResolver.setDisable   (true);
        btnVerServicio.setDisable(true);
        cargarIncidencias();
    }

    // ─── ABRIR RESOLVER INCIDENCIA (pantalla 7) ───────────────────
    @FXML
    public void fnAbrirResolverIncidencia(ActionEvent event) {
        Incidencia sel = tvIncidencias.getSelectionModel().getSelectedItem();
        if (sel == null) {
            JOptionPane.showMessageDialog(null,
                    "Seleccione una incidencia de la tabla.");
            return;
        }
        Sesion.idIncidencia      = Integer.parseInt(sel.getIdIncidencia());
        Sesion.idServicioPintura = Integer.parseInt(sel.getIdServicio());
        Sesion.estadoServicio    = "en_correccion";

        navegarA("/com/example/puestosypintado/pintura/ResolverIncidencia.fxml", event);
    }

    // ─── ABRIR DETALLE DEL SERVICIO (pantalla 4) ──────────────────
    @FXML
    public void fnAbrirDetalleServicio(ActionEvent event) {
        Incidencia sel = tvIncidencias.getSelectionModel().getSelectedItem();
        if (sel == null) {
            JOptionPane.showMessageDialog(null,
                    "Seleccione una incidencia de la tabla.");
            return;
        }
        Sesion.idServicioPintura = Integer.parseInt(sel.getIdServicio());

        navegarA("/com/example/puestosypintado/pintura/DetalleServicio.fxml", event);
    }

    // ─── HOME ─────────────────────────────────────────────────────
    @FXML
    public void irHome(ActionEvent event) {
        navegarA("/com/example/puestosypintado/General/Home.fxml", event);
    }

    private void navegarA(String fxml, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}