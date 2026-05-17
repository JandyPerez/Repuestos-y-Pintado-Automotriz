package com.example.puestosypintado.controladores;

import com.example.puestosypintado.Database.Conexion;
import com.example.puestosypintado.modelo.CompatibleVehiculo;
import com.example.puestosypintado.modelo.Poducto;
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

public class ProductoController {

    Conexion conexion = new Conexion();

    // ─── FXML — Tabla principal ───────────────────────────────────
    @FXML private TableView<Poducto>            tvInventario;
    @FXML private TableColumn<Poducto, String>  colSKU;
    @FXML private TableColumn<Poducto, String>  colDescripcion;   // columna "NOMBRE" en pantalla
    @FXML private TableColumn<Poducto, String>  colStock;

    // ─── FXML — Campos originales ─────────────────────────────────
    @FXML private TextField        txtSKU;          // ID (deshabilitado — IDENTITY)
    @FXML private TextField        txtnombre;
    @FXML private TextField        txtDescripcion;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private TextField        txtStock;
    @FXML private TextField        txtStockMinimo;
    @FXML private TextField        txtPrecioCom;
    @FXML private TextField        txtPrecio;

    // ─── FXML — Campos nuevos ─────────────────────────────────────
    @FXML private ComboBox<String> cmbTipoVehiculo;
    @FXML private ComboBox<String> cmbEstadoPieza;
    @FXML private TextField        txtColor;
    @FXML private TextField        txtPaisOrigen;
    @FXML private TextField        txtAnioPieza;
    @FXML private TextField        txtMarcaPieza;
    @FXML private TextField        txtModeloPieza;

    // ─── FXML — Tabla de vehículos compatibles ────────────────────
    @FXML private TableView<CompatibleVehiculo>            tvCompatibles;
    @FXML private TableColumn<CompatibleVehiculo, String>  colCompatMarca;
    @FXML private TableColumn<CompatibleVehiculo, String>  colCompatModelo;
    @FXML private TableColumn<CompatibleVehiculo, String>  colCompatAnio;

    // Mini-form para agregar vehículo compatible
    @FXML private TextField txtCompatMarca;
    @FXML private TextField txtCompatModelo;
    @FXML private TextField txtCompatAnio;

    // Lista en memoria de compatibles (para nuevos productos aún sin sku)
    private final ObservableList<CompatibleVehiculo> listaCompatibles =
            FXCollections.observableArrayList();

    // ─── INITIALIZE ──────────────────────────────────────────────
    public void initialize() {

        // Categorías desde BD
        cmbCategoria.setItems(FXCollections.observableArrayList(
                "Accesorios", "No me acuerdo los otros", "algo mas pa decorar"
        ));
        //cmbCategoria.setValue(null);

        // Estado de la pieza
        cmbEstadoPieza.setItems(FXCollections.observableArrayList(
                "Nueva", "Usada", "Remanufacturada"
        ));

        // Tipos de vehículo
        cmbTipoVehiculo.setItems(FXCollections.observableArrayList(
                "Automovil", "Camioneta", "Camion", "Motocicleta",
                "Autobus", "Van", "SUV", "Pickup"
        ));

        // FIX #7: setCellValueFactory SOLO en initialize, no en actualizarLista
        colSKU.setCellValueFactory        (cell -> cell.getValue().skuProperty());
        colDescripcion.setCellValueFactory(cell -> cell.getValue().nombreProperty()); // FIX #1
        colStock.setCellValueFactory      (cell -> cell.getValue().stockProperty());

        // Columnas de la tabla de compatibles
        colCompatMarca.setCellValueFactory (cell -> cell.getValue().marcaProperty());
        colCompatModelo.setCellValueFactory(cell -> cell.getValue().modeloProperty());
        colCompatAnio.setCellValueFactory  (cell -> cell.getValue().anioProperty());

        tvCompatibles.setItems(listaCompatibles);

        actualizarLista();

        // FIX #8: clic en fila → alerta con id/nombre + rellena formulario
        tvInventario.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        JOptionPane.showMessageDialog(
                                null,
                                "Producto seleccionado:\n"
                                        + "  ID    : " + newVal.getSku() + "\n"
                                        + "  Nombre: " + newVal.getNombre(),
                                "Producto cargado",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        buscarPorSku(Integer.parseInt(newVal.getSku()));
                    }
                });
    }

    // ─── CARGAR CATEGORÍAS ────────────────────────────────────────
//    private ObservableList<String> fillComboBoxCategoria() {
//
//        ObservableList<String> lista = FXCollections.observableArrayList();
//
//        String sql = "SELECT nombre_empresa FROM [tbl.Proveedor] ORDER BY nombre";
//
//        try (Connection conn = conexion.estabecerConexion();
//             PreparedStatement ps = conn.prepareStatement(sql);
//             ResultSet rs = ps.executeQuery()) {
//
//            while (rs.next()) lista.add(rs.getString("nombre"));
//
//        } catch (Exception e) {
//            JOptionPane.showMessageDialog(null,
//                    "Error cargando categorias: " + e.getMessage());
//        }
//
//        return lista;
//    }

    // Obtiene el id de una categoría por nombre
    private int obtenerIdCategoria(String nombre) {

        if (nombre == null || nombre.isBlank()) return -1;

        String sql = "SELECT id FROM Categoria WHERE nombre = ?";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error obteniendo categoria: " + e.getMessage());
        }

        return -1;
    }

    // Obtiene el nombre de una categoría por id
    private String getCategoriaNombre(int id) {

        String sql = "SELECT nombre FROM Categoria WHERE id = ?";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("nombre");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error obteniendo categoria: " + e.getMessage());
        }

        return null;
    }

    // ─── LISTA PRINCIPAL (TableView) ──────────────────────────────
    // FIX #1: lee "nombre" (no "descripcion") para la columna NOMBRE
    protected ObservableList<Poducto> observableProducto() {

        ObservableList<Poducto> lista = FXCollections.observableArrayList();

        String sql = "SELECT sku, nombre, stock_actual FROM Producto";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Poducto(
                        rs.getString("sku"),
                        rs.getString("nombre"),       // FIX #1
                        rs.getString("stock_actual")
                ));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error al cargar lista: " + e.getMessage());
        }

        return lista;
    }

    // FIX #7: ya no re-declara cellValueFactory
    public void actualizarLista() {
        tvInventario.setItems(observableProducto());
    }

    // ─── GUARDAR ─────────────────────────────────────────────────
    // Incluye los 7 campos nuevos. Obtiene el sku generado para
    // luego insertar los vehículos compatibles.
    @FXML
    public void fnGuardarProducto(ActionEvent event) {

        String nombre = txtnombre.getText().trim();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(null, "El nombre del producto es obligatorio.");
            return;
        }

        int idCategoria = obtenerIdCategoria(cmbCategoria.getValue());
        if (idCategoria == -1) {
            JOptionPane.showMessageDialog(null, "Seleccione una categoria.");
            return;
        }

        double precioCompra;
        double precioVenta;
        try {
            precioCompra = Double.parseDouble(txtPrecioCom.getText().trim()); // FIX #4
            precioVenta  = Double.parseDouble(txtPrecio.getText().trim());    // FIX #4
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Los precios deben ser valores numericos.");
            return;
        }

        if (precioVenta <= precioCompra) {
            JOptionPane.showMessageDialog(null,
                    "El precio de venta debe ser mayor que el precio de compra.");
            return;
        }

        int stockActual;
        int stockMinimo;
        try {
            stockActual = Integer.parseInt(txtStock.getText().trim());
            stockMinimo = Integer.parseInt(txtStockMinimo.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Stock debe ser un numero entero.");
            return;
        }

        String sql = "INSERT INTO Producto "
                + "(categoria_id, nombre, descripcion, precio_compra, precio_venta, "
                + " stock_actual, stock_minimo, tipo_vehiculo, estado_pieza, color, "
                + " pais_origen, anio_pieza, marca_pieza, modelo_pieza) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {    // para obtener sku generado

            ps.setInt   (1,  idCategoria);
            ps.setString(2,  nombre);
            ps.setString(3,  txtDescripcion.getText().trim());
            ps.setDouble(4,  precioCompra);
            ps.setDouble(5,  precioVenta);
            ps.setInt   (6,  stockActual);
            ps.setInt   (7,  stockMinimo);
            ps.setString(8,  cmbTipoVehiculo.getValue());
            ps.setString(9,  cmbEstadoPieza.getValue());
            ps.setString(10, txtColor.getText().trim());
            ps.setString(11, txtPaisOrigen.getText().trim());

            String anioPiezaStr = txtAnioPieza.getText().trim();
            if (!anioPiezaStr.isEmpty()) {
                ps.setInt(12, Integer.parseInt(anioPiezaStr));
            } else {
                ps.setNull(12, Types.INTEGER);
            }

            ps.setString(13, txtMarcaPieza.getText().trim());
            ps.setString(14, txtModeloPieza.getText().trim());

            if (ps.executeUpdate() == 1) {

                // Obtener el sku generado por IDENTITY
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    int skuGenerado = keys.getInt(1);
                    guardarCompatibles(conn, skuGenerado);   // guarda lista en memoria
                }

                JOptionPane.showMessageDialog(null, "Producto guardado correctamente.");
                actualizarLista();
                fnLimpiar(null);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "El campo ANIO debe contener solo numeros.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al guardar: " + e.getMessage());
        }
    }

    // ─── MODIFICAR ───────────────────────────────────────────────
    // FIX #4: parseDouble en precios
    // FIX #5: validación con return temprano (no if/else roto)
    @FXML
    public void fnModificarProducto(ActionEvent event) {

        if (txtSKU.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Busque un producto primero.");
            return;
        }

        int idCategoria = obtenerIdCategoria(cmbCategoria.getValue());
        if (idCategoria == -1) {
            JOptionPane.showMessageDialog(null, "Seleccione una categoria.");
            return;
        }

        double precioCompra;
        double precioVenta;
        try {
            precioCompra = Double.parseDouble(txtPrecioCom.getText().trim()); // FIX #4
            precioVenta  = Double.parseDouble(txtPrecio.getText().trim());    // FIX #4
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Los precios deben ser valores numericos.");
            return;
        }

        // FIX #5: validación con return, no if/else que dejaba executeUpdate suelto
        if (precioVenta <= precioCompra) {
            JOptionPane.showMessageDialog(null,
                    "El precio de venta debe ser mayor que el precio de compra.");
            return;
        }

        int stockActual;
        int stockMinimo;
        try {
            stockActual = Integer.parseInt(txtStock.getText().trim());
            stockMinimo = Integer.parseInt(txtStockMinimo.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Stock debe ser un numero entero.");
            return;
        }

        int sku = Integer.parseInt(txtSKU.getText().trim());

        String sql = "UPDATE Producto SET "
                + "categoria_id=?, nombre=?, descripcion=?, precio_compra=?, precio_venta=?, "
                + "stock_actual=?, stock_minimo=?, tipo_vehiculo=?, estado_pieza=?, color=?, "
                + "pais_origen=?, anio_pieza=?, marca_pieza=?, modelo_pieza=? "
                + "WHERE sku=?";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt   (1,  idCategoria);
            ps.setString(2,  txtnombre.getText().trim());
            ps.setString(3,  txtDescripcion.getText().trim());
            ps.setDouble(4,  precioCompra);
            ps.setDouble(5,  precioVenta);
            ps.setInt   (6,  stockActual);
            ps.setInt   (7,  stockMinimo);
            ps.setString(8,  cmbTipoVehiculo.getValue());
            ps.setString(9,  cmbEstadoPieza.getValue());
            ps.setString(10, txtColor.getText().trim());
            ps.setString(11, txtPaisOrigen.getText().trim());

            String anioPiezaStr = txtAnioPieza.getText().trim();
            if (!anioPiezaStr.isEmpty()) {
                ps.setInt(12, Integer.parseInt(anioPiezaStr));
            } else {
                ps.setNull(12, Types.INTEGER);
            }

            ps.setString(13, txtMarcaPieza.getText().trim());
            ps.setString(14, txtModeloPieza.getText().trim());
            ps.setInt   (15, sku);

            if (ps.executeUpdate() == 1) {
                // Sincronizar compatibles: borrar todos y re-insertar desde la lista
                sincronizarCompatibles(conn, sku);

                JOptionPane.showMessageDialog(null, "Producto actualizado correctamente.");
                actualizarLista();
                fnLimpiar(null);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "El campo ANIO debe contener solo numeros.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al modificar: " + e.getMessage());
        }
    }

    // ─── BORRAR ──────────────────────────────────────────────────
    // FIX #6: PreparedStatement con parámetro ? (sin concatenación)
    @FXML
    public void fnBorrarProducto(ActionEvent event) {

        if (txtSKU.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Busque un producto primero.");
            return;
        }

        int sku = Integer.parseInt(txtSKU.getText().trim());
        String nombre = txtnombre.getText().trim();

        // Verificar si el producto tiene ventas registradas en DetalleVenta
        // antes de intentar borrarlo (evita el FK constraint violation)
        if (tieneVentasAsociadas(sku)) {
            JOptionPane.showMessageDialog(
                    null,
                    "No se puede eliminar el producto '" + nombre + "'\n"
                            + "porque tiene ventas registradas en el sistema.\n\n"
                            + "Debe eliminar primero los registros de venta asociados,\n"
                            + "o desactivar el producto en lugar de borrarlo.",
                    "Eliminacion bloqueada",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Alert alerta = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Seguro que desea eliminar: " + nombre + "?",
                ButtonType.YES, ButtonType.NO);
        alerta.setTitle("Confirmar eliminacion");

        Optional<ButtonType> resultado = alerta.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.YES) {

            String sql = "DELETE FROM Producto WHERE sku = ?";

            try (Connection conn = conexion.estabecerConexion();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, sku);

                if (ps.executeUpdate() == 1) {
                    JOptionPane.showMessageDialog(null,
                            "Producto eliminado correctamente.");
                    actualizarLista();
                    fnLimpiar(null);
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Error al borrar: " + e.getMessage());
            }
        }
    }

    // Devuelve true si el producto tiene filas en DetalleVenta
    private boolean tieneVentasAsociadas(int sku) {

        String sql = "SELECT TOP 1 1 FROM DetalleVenta WHERE repuesto_id = ?";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sku);
            return ps.executeQuery().next();   // true si hay al menos 1 fila

        } catch (Exception e) {
            // Si la tabla no existe o falla la consulta, no bloqueamos
            return false;
        }
    }

    // ─── BUSCAR (botón ? junto a txtSKU) ─────────────────────────
    // FIX #2/#3: PreparedStatement con parámetro, busca por SKU (ID)
    @FXML
    public void fnBuscarProducto(ActionEvent event) {

        String skuStr  = txtSKU.getText().trim();
        String nombre  = txtnombre.getText().trim();

        if (!skuStr.isEmpty()) {
            try {
                buscarPorSku(Integer.parseInt(skuStr));  // FIX #2: parámetro tipado
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "El ID debe ser un numero entero.");
            }

        } else if (!nombre.isEmpty()) {
            buscarPorNombre(nombre);

        } else {
            JOptionPane.showMessageDialog(null,
                    "Ingrese el ID o el nombre del producto para buscar.");
        }
    }

    // Busca por sku usando PreparedStatement seguro — FIX #2/#3
    private void buscarPorSku(int sku) {

        String sql = "SELECT * FROM Producto WHERE sku = ?";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sku);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                rellenarFormulario(rs);
                cargarCompatibles(sku);
            } else {
                JOptionPane.showMessageDialog(null,
                        "No se encontro ningun producto con ese ID.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error al buscar: " + e.getMessage());
        }
    }

    // Busca por nombre usando PreparedStatement seguro — FIX #2/#3
    private void buscarPorNombre(String nombre) {

        String sql = "SELECT * FROM Producto WHERE nombre LIKE ?";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + nombre + "%");
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                rellenarFormulario(rs);
                cargarCompatibles(rs.getInt("sku"));
            } else {
                JOptionPane.showMessageDialog(null,
                        "No se encontro ningun producto con ese nombre.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error al buscar: " + e.getMessage());
        }
    }

    // Rellena el formulario completo desde un ResultSet
    private void rellenarFormulario(ResultSet rs) throws SQLException {

        txtSKU.setText        (rs.getString("sku"));
        txtnombre.setText     (rs.getString("nombre"));
        txtDescripcion.setText(rs.getString("descripcion"));
        txtStock.setText      (rs.getString("stock_actual"));
        txtStockMinimo.setText(rs.getString("stock_minimo"));
        txtPrecioCom.setText  (rs.getString("precio_compra"));
        txtPrecio.setText     (rs.getString("precio_venta"));

        // Campos nuevos
        cmbTipoVehiculo.setValue(rs.getString("tipo_vehiculo"));
        cmbEstadoPieza.setValue (rs.getString("estado_pieza"));
        txtColor.setText        (rs.getString("color"));
        txtPaisOrigen.setText   (rs.getString("pais_origen"));

        int anioPieza = rs.getInt("anio_pieza");
        txtAnioPieza.setText(rs.wasNull() ? "" : String.valueOf(anioPieza));

        txtMarcaPieza.setText (rs.getString("marca_pieza"));
        txtModeloPieza.setText(rs.getString("modelo_pieza"));

        cmbCategoria.setValue(getCategoriaNombre(rs.getInt("categoria_id")));
    }

    // ─── VEHÍCULOS COMPATIBLES ───────────────────────────────────

    // Carga los compatibles de un producto desde la BD
    private void cargarCompatibles(int sku) {

        listaCompatibles.clear();

        String sql = "SELECT * FROM ProductoVehiculo WHERE sku_producto = ?";

        try (Connection conn = conexion.estabecerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sku);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                listaCompatibles.add(new CompatibleVehiculo(
                        rs.getInt   ("id"),
                        rs.getInt   ("sku_producto"),
                        rs.getString("marca"),
                        rs.getString("modelo"),
                        rs.getString("anio") == null ? "" : rs.getString("anio")
                ));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error al cargar compatibles: " + e.getMessage());
        }
    }

    // Agrega un vehículo compatible a la lista en memoria
    // Si el producto ya existe (sku en txtSKU), también lo persiste en BD
    @FXML
    public void fnAgregarCompatible(ActionEvent event) {

        String marca  = txtCompatMarca.getText().trim();
        String modelo = txtCompatModelo.getText().trim();
        String anio   = txtCompatAnio.getText().trim();

        if (marca.isEmpty() || modelo.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Marca y Modelo son obligatorios para el vehiculo compatible.");
            return;
        }

        CompatibleVehiculo nuevo = new CompatibleVehiculo(marca, modelo, anio);

        // Si el producto ya tiene sku (fue guardado o buscado), persistir en BD
        if (!txtSKU.getText().isEmpty()) {
            int sku = Integer.parseInt(txtSKU.getText().trim());
            String sql = "INSERT INTO ProductoVehiculo (sku_producto, marca, modelo, anio) "
                    + "VALUES (?, ?, ?, ?)";

            try (Connection conn = conexion.estabecerConexion();
                 PreparedStatement ps = conn.prepareStatement(
                         sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt   (1, sku);
                ps.setString(2, marca);
                ps.setString(3, modelo);

                if (!anio.isEmpty()) {
                    ps.setInt(4, Integer.parseInt(anio));
                } else {
                    ps.setNull(4, Types.INTEGER);
                }

                ps.executeUpdate();

                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    nuevo.setId(keys.getInt(1));
                    nuevo.setSkuProducto(sku);
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null,
                        "El campo ANIO del vehiculo compatible debe ser un numero.");
                return;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Error al agregar compatible: " + e.getMessage());
                return;
            }
        }

        listaCompatibles.add(nuevo);
        txtCompatMarca.clear();
        txtCompatModelo.clear();
        txtCompatAnio.clear();
    }

    // Quita el vehículo compatible seleccionado en la tabla
    @FXML
    public void fnQuitarCompatible(ActionEvent event) {

        CompatibleVehiculo seleccionado =
                tvCompatibles.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            JOptionPane.showMessageDialog(null,
                    "Seleccione un vehiculo compatible para eliminar.");
            return;
        }

        // Si ya fue persistido en BD, eliminarlo
        if (seleccionado.getId() > 0) {
            String sql = "DELETE FROM ProductoVehiculo WHERE id = ?";

            try (Connection conn = conexion.estabecerConexion();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, seleccionado.getId());
                ps.executeUpdate();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Error al quitar compatible: " + e.getMessage());
                return;
            }
        }

        listaCompatibles.remove(seleccionado);
    }

    // Inserta todos los compatibles en memoria para un producto recién creado
    private void guardarCompatibles(Connection conn, int skuNuevo) throws SQLException {

        if (listaCompatibles.isEmpty()) return;

        String sql = "INSERT INTO ProductoVehiculo (sku_producto, marca, modelo, anio) "
                + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (CompatibleVehiculo cv : listaCompatibles) {
                ps.setInt   (1, skuNuevo);
                ps.setString(2, cv.getMarca());
                ps.setString(3, cv.getModelo());

                if (!cv.getAnio().isEmpty()) {
                    ps.setInt(4, Integer.parseInt(cv.getAnio()));
                } else {
                    ps.setNull(4, Types.INTEGER);
                }

                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // Borra y re-inserta todos los compatibles al editar un producto
    private void sincronizarCompatibles(Connection conn, int sku) throws SQLException {

        // Borrar todos los existentes
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM ProductoVehiculo WHERE sku_producto = ?")) {
            ps.setInt(1, sku);
            ps.executeUpdate();
        }

        // Re-insertar desde la lista en memoria
        guardarCompatibles(conn, sku);
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
    public void fnLimpiar(ActionEvent event) {

        txtSKU.clear();
        txtnombre.clear();
        txtDescripcion.clear();
        txtStock.clear();
        txtStockMinimo.clear();
        txtPrecioCom.clear();
        txtPrecio.clear();

        // Campos nuevos
        txtColor.clear();
        txtPaisOrigen.clear();
        txtAnioPieza.clear();
        txtMarcaPieza.clear();
        txtModeloPieza.clear();

        cmbCategoria.setValue(null);
        cmbTipoVehiculo.setValue(null);
        cmbEstadoPieza.setValue(null);

        // Mini-form compatibles
        txtCompatMarca.clear();
        txtCompatModelo.clear();
        txtCompatAnio.clear();

        listaCompatibles.clear();
        tvInventario.getSelectionModel().clearSelection();
    }
}