package com.example.puestosypintado.modelo;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Modelo del catálogo de subservicios de pintura.
 * Tabla: [tbl.Subservicios]
 */
public class Subservicio {

    private final StringProperty idSubservicio = new SimpleStringProperty();
    private final StringProperty nombre        = new SimpleStringProperty();
    private final StringProperty descripcion   = new SimpleStringProperty();
    private final StringProperty precioBase    = new SimpleStringProperty();
    private final StringProperty estado        = new SimpleStringProperty();

    // Constructor completo
    public Subservicio(String idSubservicio, String nombre,
                       String descripcion, String precioBase, String estado) {
        this.idSubservicio.set(idSubservicio);
        this.nombre.set(nombre);
        this.descripcion.set(descripcion);
        this.precioBase.set(precioBase);
        this.estado.set(estado);
    }

    // Getters
    public String getIdSubservicio() { return idSubservicio.get(); }
    public String getNombre()        { return nombre.get(); }
    public String getDescripcion()   { return descripcion.get(); }
    public String getPrecioBase()    { return precioBase.get(); }
    public String getEstado()        { return estado.get(); }

    // Properties
    public StringProperty idSubservicioProperty() { return idSubservicio; }
    public StringProperty nombreProperty()        { return nombre; }
    public StringProperty descripcionProperty()   { return descripcion; }
    public StringProperty precioBaseProperty()    { return precioBase; }
    public StringProperty estadoProperty()        { return estado; }

    @Override
    public String toString() { return nombre.get(); }  // Para ComboBox
}