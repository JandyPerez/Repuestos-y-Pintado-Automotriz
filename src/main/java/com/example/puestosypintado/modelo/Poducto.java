package com.example.puestosypintado.modelo;

import javafx.beans.property.*;

/**
 * Modelo completo del Producto.
 * Incluye todos los campos de la tabla Producto (originales + nuevos).
 * Nota: el nombre de clase "Poducto" se mantiene para no romper referencias existentes.
 */
public class Poducto {

    // ─── Propiedades originales ──────────────────────────────────
    private final StringProperty  sku           = new SimpleStringProperty();
    private final StringProperty  nombre        = new SimpleStringProperty();
    private final StringProperty  stock         = new SimpleStringProperty();
    private final StringProperty  descripcion   = new SimpleStringProperty();
    private final StringProperty  categoriaId   = new SimpleStringProperty();
    private final StringProperty  stockMinimo   = new SimpleStringProperty();
    private final StringProperty  precioCompra  = new SimpleStringProperty();
    private final StringProperty  precioVenta   = new SimpleStringProperty();

    // ─── Propiedades nuevas ──────────────────────────────────────
    private final StringProperty  tipoVehiculo  = new SimpleStringProperty();
    private final StringProperty  estadoPieza   = new SimpleStringProperty();
    private final StringProperty  color         = new SimpleStringProperty();
    private final StringProperty  paisOrigen    = new SimpleStringProperty();
    private final StringProperty  anioPieza     = new SimpleStringProperty();
    private final StringProperty  marcaPieza    = new SimpleStringProperty();
    private final StringProperty  modeloPieza   = new SimpleStringProperty();

    // ─── Constructor para TableView (lista) ─────────────────────
    public Poducto(String sku, String nombre, String stock) {
        this.sku.set(sku);
        this.nombre.set(nombre);
        this.stock.set(stock);
    }

    // ─── Constructor completo (formulario) ──────────────────────
    public Poducto(String sku, String nombre, String descripcion,
                   String categoriaId, String stock, String stockMinimo,
                   String precioCompra, String precioVenta,
                   String tipoVehiculo, String estadoPieza, String color,
                   String paisOrigen, String anioPieza,
                   String marcaPieza, String modeloPieza) {

        this.sku.set(sku);
        this.nombre.set(nombre);
        this.descripcion.set(descripcion);
        this.categoriaId.set(categoriaId);
        this.stock.set(stock);
        this.stockMinimo.set(stockMinimo);
        this.precioCompra.set(precioCompra);
        this.precioVenta.set(precioVenta);
        this.tipoVehiculo.set(tipoVehiculo);
        this.estadoPieza.set(estadoPieza);
        this.color.set(color);
        this.paisOrigen.set(paisOrigen);
        this.anioPieza.set(anioPieza);
        this.marcaPieza.set(marcaPieza);
        this.modeloPieza.set(modeloPieza);
    }

    // ─── Getters ─────────────────────────────────────────────────
    public String getSku()          { return sku.get(); }
    public String getNombre()       { return nombre.get(); }
    public String getDescripcion()  { return descripcion.get(); }
    public String getCategoriaId()  { return categoriaId.get(); }
    public String getStock()        { return stock.get(); }
    public String getStockMinimo()  { return stockMinimo.get(); }
    public String getPrecioCompra() { return precioCompra.get(); }
    public String getPrecioVenta()  { return precioVenta.get(); }
    public String getTipoVehiculo() { return tipoVehiculo.get(); }
    public String getEstadoPieza()  { return estadoPieza.get(); }
    public String getColor()        { return color.get(); }
    public String getPaisOrigen()   { return paisOrigen.get(); }
    public String getAnioPieza()    { return anioPieza.get(); }
    public String getMarcaPieza()   { return marcaPieza.get(); }
    public String getModeloPieza()  { return modeloPieza.get(); }

    // ─── Setters ─────────────────────────────────────────────────
    public void setSku         (String v) { sku.set(v); }
    public void setNombre      (String v) { nombre.set(v); }
    public void setDescripcion (String v) { descripcion.set(v); }
    public void setCategoriaId (String v) { categoriaId.set(v); }
    public void setStock       (String v) { stock.set(v); }
    public void setStockMinimo (String v) { stockMinimo.set(v); }
    public void setPrecioCompra(String v) { precioCompra.set(v); }
    public void setPrecioVenta (String v) { precioVenta.set(v); }
    public void setTipoVehiculo(String v) { tipoVehiculo.set(v); }
    public void setEstadoPieza (String v) { estadoPieza.set(v); }
    public void setColor       (String v) { color.set(v); }
    public void setPaisOrigen  (String v) { paisOrigen.set(v); }
    public void setAnioPieza   (String v) { anioPieza.set(v); }
    public void setMarcaPieza  (String v) { marcaPieza.set(v); }
    public void setModeloPieza (String v) { modeloPieza.set(v); }

    // ─── Properties ──────────────────────────────────────────────
    public StringProperty skuProperty()          { return sku; }
    public StringProperty nombreProperty()       { return nombre; }
    public StringProperty descripcionProperty()  { return descripcion; }
    public StringProperty categoriaIdProperty()  { return categoriaId; }
    public StringProperty stockProperty()        { return stock; }
    public StringProperty stockMinimoProperty()  { return stockMinimo; }
    public StringProperty precioCompraProperty() { return precioCompra; }
    public StringProperty precioVentaProperty()  { return precioVenta; }
    public StringProperty tipoVehiculoProperty() { return tipoVehiculo; }
    public StringProperty estadoPiezaProperty()  { return estadoPieza; }
    public StringProperty colorProperty()        { return color; }
    public StringProperty paisOrigenProperty()   { return paisOrigen; }
    public StringProperty anioPiezaProperty()    { return anioPieza; }
    public StringProperty marcaPiezaProperty()   { return marcaPieza; }
    public StringProperty modeloPiezaProperty()  { return modeloPieza; }
}