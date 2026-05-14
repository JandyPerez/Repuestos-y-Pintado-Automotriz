package com.example.puestosypintado.modelo;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Modelo del detalle de subservicios de un servicio de pintura.
 * JOIN: [tbl.Detalle_Servicio_Pintura] + [tbl.Subservicios] + [tbl.Empleado]
 */
public class DetalleSubservicio {

    private final StringProperty idDetalle    = new SimpleStringProperty();
    private final StringProperty nombre       = new SimpleStringProperty();  // tbl.Subservicios.nombre
    private final StringProperty precioBase   = new SimpleStringProperty();  // tbl.Subservicios.precio_base
    private final StringProperty estado       = new SimpleStringProperty();  // tbl.Detalle_Servicio_Pintura.estado
    private final StringProperty tecnico      = new SimpleStringProperty();  // tbl.Empleado.nombre_completo
    private final StringProperty fechaFin     = new SimpleStringProperty();  // fecha_fin formateada
    private final StringProperty observaciones= new SimpleStringProperty();

    public DetalleSubservicio(String idDetalle, String nombre, String precioBase,
                              String estado, String tecnico,
                              String fechaFin, String observaciones) {
        this.idDetalle.set(idDetalle);
        this.nombre.set(nombre);
        this.precioBase.set(precioBase);
        this.estado.set(estado);
        this.tecnico.set(tecnico);
        this.fechaFin.set(fechaFin);
        this.observaciones.set(observaciones);
    }

    // Getters
    public String getIdDetalle()     { return idDetalle.get(); }
    public String getNombre()        { return nombre.get(); }
    public String getPrecioBase()    { return precioBase.get(); }
    public String getEstado()        { return estado.get(); }
    public String getTecnico()       { return tecnico.get(); }
    public String getFechaFin()      { return fechaFin.get(); }
    public String getObservaciones() { return observaciones.get(); }

    // Properties
    public StringProperty idDetalleProperty()    { return idDetalle; }
    public StringProperty nombreProperty()       { return nombre; }
    public StringProperty precioBaseProperty()   { return precioBase; }
    public StringProperty estadoProperty()       { return estado; }
    public StringProperty tecnicoProperty()      { return tecnico; }
    public StringProperty fechaFinProperty()     { return fechaFin; }
    public StringProperty observacionesProperty(){ return observaciones; }
}