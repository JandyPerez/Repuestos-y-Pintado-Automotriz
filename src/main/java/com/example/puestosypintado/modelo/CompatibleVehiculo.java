package com.example.puestosypintado.modelo;

import javafx.beans.property.*;

/**
 * Representa un vehículo compatible con un producto (tabla ProductoVehiculo).
 */
public class CompatibleVehiculo {

    private final IntegerProperty id           = new SimpleIntegerProperty();
    private final IntegerProperty skuProducto  = new SimpleIntegerProperty();
    private final StringProperty  marca        = new SimpleStringProperty();
    private final StringProperty  modelo       = new SimpleStringProperty();
    private final StringProperty  anio         = new SimpleStringProperty();

    // ─── Constructor para lista desde BD ────────────────────────
    public CompatibleVehiculo(int id, int skuProducto,
                              String marca, String modelo, String anio) {
        this.id.set(id);
        this.skuProducto.set(skuProducto);
        this.marca.set(marca);
        this.modelo.set(modelo);
        this.anio.set(anio);
    }

    // ─── Constructor para nuevas entradas (aún sin ID) ───────────
    public CompatibleVehiculo(String marca, String modelo, String anio) {
        this.id.set(0);
        this.skuProducto.set(0);
        this.marca.set(marca);
        this.modelo.set(modelo);
        this.anio.set(anio);
    }

    // ─── Getters ─────────────────────────────────────────────────
    public int    getId()          { return id.get(); }
    public int    getSkuProducto() { return skuProducto.get(); }
    public String getMarca()       { return marca.get(); }
    public String getModelo()      { return modelo.get(); }
    public String getAnio()        { return anio.get(); }

    // ─── Setters ─────────────────────────────────────────────────
    public void setId         (int    v) { id.set(v); }
    public void setSkuProducto(int    v) { skuProducto.set(v); }
    public void setMarca      (String v) { marca.set(v); }
    public void setModelo     (String v) { modelo.set(v); }
    public void setAnio       (String v) { anio.set(v); }

    // ─── Properties ──────────────────────────────────────────────
    public IntegerProperty idProperty()          { return id; }
    public IntegerProperty skuProductoProperty() { return skuProducto; }
    public StringProperty  marcaProperty()       { return marca; }
    public StringProperty  modeloProperty()      { return modelo; }
    public StringProperty  anioProperty()        { return anio; }
}