package com.example.puestosypintado.controladores;

import com.example.puestosypintado.Database.Conexion;
import com.example.puestosypintado.modelo.DetalleSubservicio;
import com.example.puestosypintado.modelo.Incidencia;
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
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import javax.swing.JOptionPane;
import java.sql.*;
import java.util.List;
import java.util.Optional;

/**
 * Controlador: Pantalla 4 — Detalle del Servicio de Pintura
 * Lee: [tbl.Servicio_Pintura], [tbl.Cliente], [tbl.Vehiculo],
 *      [tbl.Detalle_Servicio_Pintura], [tbl.Subservicios],
 *      [tbl.Incidencias_Servicio], [tbl.Detalle_Pago_Cliente]
 * Funciones: fn_CalcularCostoTotalServicio, fn_SaldoPendienteServicio, fn_AnticipoPagado
 */
public class DetalleServicioController {

    Conexion conexion = new Conexion();

    // ─── FXML — Banda de estado ───────────────────────────────────
    @FXML private Pane  paneEstado;
    @FXML private Label lblEstado;
    @FXML private Label lblIdOrden;
    @FXML private Label lblDiasTaller;

    // ─── FXML — Datos del servicio ────────────────────────────────
    @FXML private TextField txtCliente;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtVehiculo;
    @FXML private TextField txtPlaca;
    @FXML private TextField txtFechaEntrada;
    @FXML private TextField txtFechaPrometida;
    @FXML private TextField txtFechaLimite;
    @FXML private TextField txtCostoTotal;
    @FXML private TextField txtAnticipoRequerido;
    @FXML private TextField txtPagado;
    @FXML private TextField txtSaldoPendiente;
    @FXML private Label     lblAnticipoPagado;
    @FXML private TextField txtDescripcion;

    // ─── FXML — Tabla subservicios ────────────────────────────────
    @FXML private TableView<DetalleSubservicio>           tvSubservicios;
    @FXML private TableColumn<DetalleSubservicio, String> colSubNombre;
    @FXML private TableColumn<DetalleSubservicio, String> colSubEstado;
    @FXML private TableColumn<DetalleSubservicio, String> colSubTecnico;
    @FXML private TableColumn<DetalleSubservicio, String> colSubFecFin;

    // ─── FXML — Tabla incidencias ─────────────────────────────────
    @FXML private TableView<Incidencia>           tvIncidencias;
    @FXML private TableColumn<Incidencia, String> colIncTipo;
    @FXML private TableColumn<Incidencia, String> colIncSubservicio;
    @FXML private TableColumn<Incidencia, String> colIncEstado;
    @FXML private TableColumn<Incidencia, String> colIncFecha;

    // ─── FXML — Botones de acción ─────────────────────────────────
    @FXML private Button btnIniciarServicio;
    @FXML private Button btnFinalizar;
    @FXML private Button btnRegistrarIncidencia;
    @FXML private Button btnResolverIncidencia;
    @FXML private Button btnEntregar;
    @FXML private Button btnActualizarSubservicio;

    // ─── INITIALIZE ──────────────────────────────────────────────
    public void initialize() {

        colSubNombre.setCellValueFactory  (c -> c.getValue().nombreProperty());
        colSubEstado.setCellValueFactory  (c -> c.getValue().estadoProperty());
        colSubTecnico.setCellValueFactory (c -> c.getValue().tecnicoProperty());
        colSubFecFin.setCellValueFactory  (c -> c.getValue().fechaFinProperty());

        colIncTipo.setCellValueFactory        (c -> c.getValue().tipoDefectoProperty());
        colIncSubservicio.setCellValueFactory  (c -> c.getValue().subservicioAfectadoProperty());
        colIncEstado.setCellValueFactory       (c -> c.getValue().estadoProperty());
        colIncFecha.setCellValueFactory        (c -> c.getValue().fechaDeteccionProperty());

        cargarServicio();
    }

    // ─── CARGAR DATOS DEL SERVICIO ────────────────────────────────
    private void cargarServicio() {
        if (Sesion.idServicioPintura == 0) return;

        String sql =
                "SELECT s.id_servicioPintura, s.estado, s.descripcion_trabajo, "
                        + "       s.fecha_entrada, s.fecha_prometida, s.fecha_limite_pago, "
                        + "       s.porcentaje_anticipo, s.monto_cotizado, "
                        + "       DATEDIFF(DAY, s.fecha_entrada, GETDATE()) AS dias_taller, "
                        + "       c.nombre AS cliente, c.telefono, "
                        + "       v.marca + ' ' + v.modelo AS vehiculo, v.placa, "
                        + "       dbo.fn_CalcularCostoTotalServicio(s.id_servicioPintura) AS costo_total, "
                        + "       dbo.fn_SaldoPendienteServicio(s.id_servicioPintura)    AS saldo, "
                        + "       dbo.fn_AnticipoPagado(s.id_servicioPintura)            AS anticipo_ok, "
                        + "       ISNULL(SUM(pc.total), 0) AS pagado "
                        + "FROM [tbl.Servicio_Pintura] s "
                        + "JOIN [tbl.Cliente]  c ON s.fk_cliente  = c.id_cliente "
                        + "JOIN [tbl.Vehiculo] v ON s.fk_vehiculo = v.id_vehiculo "
                        + "LEFT JOIN [tbl.Detalle_Pago_Cliente] pc "
                        + "     ON pc.fk_servicioPintura = s.id_servicioPintura AND pc.estado = 'pagado' "
                        + "WHERE s.id_servicioPintura = ? "
                        + "GROUP BY s.id_servicioPintura, s.estado, s.descripcion_trabajo, "
                        + "         s.fecha_entrada, s.fecha_prometida, s.fecha_limite_pago, "
                        + "         s.porcentaje_anticipo, s.monto_cotizado, c.nombre, c.telefono, "
                        + "         v.marca, v.modelo, v.placa";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Sesion.idServicioPintura);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String estado = rs.getString("estado");
                Sesion.estadoServicio = estado;

                lblIdOrden.setText   (rs.getString("id_servicioPintura"));
                lblEstado.setText    (estado.toUpperCase());
                lblDiasTaller.setText(rs.getString("dias_taller") + " días");

                txtCliente.setText  (rs.getString("cliente"));
                txtTelefono.setText (rs.getString("telefono") == null ? "" : rs.getString("telefono"));
                txtVehiculo.setText (rs.getString("vehiculo"));
                txtPlaca.setText    (rs.getString("placa"));

                txtFechaEntrada.setText  (formatFecha(rs.getTimestamp("fecha_entrada")));
                txtFechaPrometida.setText(formatFecha(rs.getDate("fecha_prometida")));
                txtFechaLimite.setText   (formatFecha(rs.getDate("fecha_limite_pago")));

                double costoTotal  = rs.getDouble("costo_total");
                double pctAnticipo = rs.getDouble("porcentaje_anticipo");
                double pagado      = rs.getDouble("pagado");
                double saldo       = rs.getDouble("saldo");
                boolean anticipoOk = rs.getInt("anticipo_ok") == 1;

                txtCostoTotal.setText      (String.format("%.2f", costoTotal));
                txtAnticipoRequerido.setText(String.format("%.2f", costoTotal * pctAnticipo / 100.0));
                txtPagado.setText          (String.format("%.2f", pagado));
                txtSaldoPendiente.setText  (String.format("%.2f", saldo));
                lblAnticipoPagado.setText  (anticipoOk ? "✓ CUBIERTO" : "✗ PENDIENTE");
                lblAnticipoPagado.setStyle (anticipoOk
                        ? "-fx-text-fill: #00AA77; -fx-font-weight: bold; -fx-font-family: 'Courier New';"
                        : "-fx-text-fill: #CC3300; -fx-font-weight: bold; -fx-font-family: 'Courier New';");

                txtDescripcion.setText(rs.getString("descripcion_trabajo"));

                configurarBandaEstado(estado);
                configurarBotones(estado, saldo);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error cargando servicio: " + e.getMessage());
        }

        cargarSubservicios();
        cargarIncidencias();
    }

    // Color de la banda según estado
    private void configurarBandaEstado(String estado) {
        String color = switch (estado) {
            case "cotizado"      -> "#888888";
            case "pago_pendiente"-> "#CC6600";
            case "abandonado"    -> "#CC3300";
            case "recibido"      -> "#0066CC";
            case "en_proceso"    -> "#006699";
            case "terminado"     -> "#00AA77";
            case "en_correccion" -> "#CC3300";
            case "entregado"     -> "#009966";
            default              -> "#555555";
        };
        paneEstado.setStyle("-fx-background-color: " + color + ";");
    }

    // Habilitar/deshabilitar botones según estado y saldo
    private void configurarBotones(String estado, double saldo) {
        btnIniciarServicio.setDisable     (!"recibido".equals(estado));
        btnFinalizar.setDisable           (!"en_proceso".equals(estado));
        btnActualizarSubservicio.setDisable(!"en_proceso".equals(estado));
        btnRegistrarIncidencia.setDisable (
                !List.of("en_proceso","terminado").contains(estado));
        btnResolverIncidencia.setDisable  (!"en_correccion".equals(estado));
        btnEntregar.setDisable            (
                !"terminado".equals(estado) || saldo > 0);
    }

    // ─── CARGAR SUBSERVICIOS ──────────────────────────────────────
    private void cargarSubservicios() {
        ObservableList<DetalleSubservicio> lista = FXCollections.observableArrayList();

        String sql =
                "SELECT d.id_detalleServicio, sb.nombre, sb.precio_base, d.estado, "
                        + "       ISNULL(e.nombre_completo, '--') AS tecnico, "
                        + "       ISNULL(CONVERT(VARCHAR, d.fecha_fin, 103), '--') AS fecha_fin, "
                        + "       ISNULL(d.observaciones, '') AS obs "
                        + "FROM [tbl.Detalle_Servicio_Pintura] d "
                        + "JOIN [tbl.Subservicios] sb ON d.fk_subservicio = sb.id_subservicio "
                        + "LEFT JOIN [tbl.Empleado] e ON d.fk_empleado    = e.id_empleado "
                        + "WHERE d.fk_servicioPintura = ? ORDER BY d.id_detalleServicio";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Sesion.idServicioPintura);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(new DetalleSubservicio(
                        rs.getString("id_detalleServicio"),
                        rs.getString("nombre"),
                        String.format("%.2f", rs.getDouble("precio_base")),
                        rs.getString("estado"),
                        rs.getString("tecnico"),
                        rs.getString("fecha_fin"),
                        rs.getString("obs")
                ));
            }
            tvSubservicios.setItems(lista);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error cargando subservicios: " + e.getMessage());
        }
    }

    // ─── CARGAR INCIDENCIAS ACTIVAS ───────────────────────────────
    private void cargarIncidencias() {
        ObservableList<Incidencia> lista = FXCollections.observableArrayList();

        String sql =
                "SELECT i.id_incidencia, i.fk_servicioPintura, "
                        + "       ISNULL(sb.nombre, '--') AS subservicio, "
                        + "       i.tipo_defecto, i.descripcion, "
                        + "       CONVERT(VARCHAR, i.fecha_deteccion, 103) AS fecha_det, "
                        + "       e.nombre_completo AS supervisor, i.estado "
                        + "FROM [tbl.Incidencias_Servicio] i "
                        + "LEFT JOIN [tbl.Detalle_Servicio_Pintura] d ON i.fk_detalleServicio = d.id_detalleServicio "
                        + "LEFT JOIN [tbl.Subservicios] sb ON d.fk_subservicio = sb.id_subservicio "
                        + "JOIN [tbl.Empleado] e ON i.fk_supervisor = e.id_empleado "
                        + "WHERE i.fk_servicioPintura = ? AND i.estado IN ('abierta','en_correccion') "
                        + "ORDER BY i.fecha_deteccion DESC";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Sesion.idServicioPintura);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(new Incidencia(
                        rs.getString("id_incidencia"),
                        rs.getString("fk_servicioPintura"),
                        "", "", // cliente y placa no necesarios aquí
                        rs.getString("subservicio"),
                        rs.getString("tipo_defecto"),
                        rs.getString("descripcion"),
                        rs.getString("fecha_det"),
                        rs.getString("supervisor"),
                        rs.getString("estado")
                ));
            }
            tvIncidencias.setItems(lista);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error cargando incidencias: " + e.getMessage());
        }
    }

    // ─── ACCIONES DE ESTADO ───────────────────────────────────────

    @FXML
    public void fnIniciarServicio(ActionEvent event) {
        cambiarEstado("en_proceso", event);
    }

    @FXML
    public void fnFinalizarServicio(ActionEvent event) {
        // Llama sp_FinalizarServicio
        String sql = "{call sp_FinalizarServicio(?)}";
        try (Connection conn = conexion.estabecerConexion();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt(1, Sesion.idServicioPintura);
            cs.execute();
            JOptionPane.showMessageDialog(null,
                    "Servicio finalizado. Listo para revisión de calidad.");
            cargarServicio();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al finalizar: " + e.getMessage());
        }
    }

    private void cambiarEstado(String nuevoEstado, ActionEvent event) {
        String sql = "UPDATE [tbl.Servicio_Pintura] SET estado = ? "
                + "WHERE id_servicioPintura = ?";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado);
            ps.setInt   (2, Sesion.idServicioPintura);
            ps.executeUpdate();
            Sesion.estadoServicio = nuevoEstado;
            cargarServicio();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al cambiar estado: " + e.getMessage());
        }
    }

    // ─── ACTUALIZAR SUBSERVICIO (diálogo inline) ──────────────────
    @FXML
    public void fnActualizarSubservicio(ActionEvent event) {

        DetalleSubservicio sel = tvSubservicios.getSelectionModel().getSelectedItem();
        if (sel == null) {
            JOptionPane.showMessageDialog(null,
                    "Seleccione un subservicio para actualizar.");
            return;
        }

        // Elegir nuevo estado
        List<String> opciones = List.of("en_curso", "completado", "en_correccion");
        ChoiceDialog<String> dialog = new ChoiceDialog<>(sel.getEstado(), opciones);
        dialog.setTitle("Actualizar Subservicio");
        dialog.setHeaderText("Subservicio: " + sel.getNombre());
        dialog.setContentText("Nuevo estado:");

        Optional<String> resultado = dialog.showAndWait();
        if (resultado.isEmpty()) return;

        // Llamar sp_ActualizarSubservicio con empleado de sesión
        String sql = "{call sp_ActualizarSubservicio(?, ?, ?, ?)}";

        try (Connection conn = conexion.estabecerConexion();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt   (1, Integer.parseInt(sel.getIdDetalle()));
            cs.setString(2, resultado.get());
            cs.setInt   (3, Sesion.idUsuario);
            cs.setNull  (4, Types.VARCHAR);  // observaciones opcional
            cs.execute();

            cargarServicio();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar: " + e.getMessage());
        }
    }

    // ─── NAVEGACIÓN A OTRAS PANTALLAS ─────────────────────────────

    @FXML
    public void fnAbrirRegistrarIncidencia(ActionEvent event) {
        navegarA("/com/example/puestosypintado/pintura/RegistrarIncidencia.fxml", event);
    }

    @FXML
    public void fnAbrirResolverIncidencia(ActionEvent event) {
        // Pasar el id de la incidencia seleccionada a Sesion
        Incidencia sel = tvIncidencias.getSelectionModel().getSelectedItem();
        if (sel == null) {
            JOptionPane.showMessageDialog(null,
                    "Seleccione una incidencia de la tabla para resolver.");
            return;
        }
        Sesion.idIncidencia = Integer.parseInt(sel.getIdIncidencia());
        navegarA("/com/example/puestosypintado/pintura/ResolverIncidencia.fxml", event);
    }

    @FXML
    public void fnAbrirEntrega(ActionEvent event) {
        navegarA("/com/example/puestosypintado/pintura/EntregaVehiculo.fxml", event);
    }

    @FXML
    public void fnVolver(ActionEvent event) {
        navegarA("/com/example/puestosypintado/pintura/AgregarSubservicios.fxml", event);
    }

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

    // ─── UTILIDADES ──────────────────────────────────────────────
    private String formatFecha(Object fecha) {
        if (fecha == null) return "--";
        return fecha.toString().substring(0, 10);  // yyyy-MM-dd
    }
}