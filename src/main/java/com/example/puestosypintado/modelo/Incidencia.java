package com.example.puestosypintado.modelo;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Modelo de incidencia de calidad.
 * Fuente principal: vw_IncidenciasAbiertas
 * Tabla base: [tbl.Incidencias_Servicio]
 */
public class Incidencia {

    private final StringProperty idIncidencia        = new SimpleStringProperty();
    private final StringProperty idServicio          = new SimpleStringProperty();
    private final StringProperty cliente             = new SimpleStringProperty();
    private final StringProperty placa               = new SimpleStringProperty();
    private final StringProperty subservicioAfectado = new SimpleStringProperty();
    private final StringProperty tipoDefecto         = new SimpleStringProperty();
    private final StringProperty descripcion         = new SimpleStringProperty();
    private final StringProperty fechaDeteccion      = new SimpleStringProperty();
    private final StringProperty supervisor          = new SimpleStringProperty();
    private final StringProperty estado              = new SimpleStringProperty();

    public Incidencia(String idIncidencia, String idServicio, String cliente,
                      String placa, String subservicioAfectado, String tipoDefecto,
                      String descripcion, String fechaDeteccion,
                      String supervisor, String estado) {
        this.idIncidencia.set(idIncidencia);
        this.idServicio.set(idServicio);
        this.cliente.set(cliente);
        this.placa.set(placa);
        this.subservicioAfectado.set(subservicioAfectado);
        this.tipoDefecto.set(tipoDefecto);
        this.descripcion.set(descripcion);
        this.fechaDeteccion.set(fechaDeteccion);
        this.supervisor.set(supervisor);
        this.estado.set(estado);
    }

    // Getters
    public String getIdIncidencia()        { return idIncidencia.get(); }
    public String getIdServicio()          { return idServicio.get(); }
    public String getCliente()             { return cliente.get(); }
    public String getPlaca()               { return placa.get(); }
    public String getSubservicioAfectado() { return subservicioAfectado.get(); }
    public String getTipoDefecto()         { return tipoDefecto.get(); }
    public String getDescripcion()         { return descripcion.get(); }
    public String getFechaDeteccion()      { return fechaDeteccion.get(); }
    public String getSupervisor()          { return supervisor.get(); }
    public String getEstado()              { return estado.get(); }

    // Properties
    public StringProperty idIncidenciaProperty()        { return idIncidencia; }
    public StringProperty idServicioProperty()          { return idServicio; }
    public StringProperty clienteProperty()             { return cliente; }
    public StringProperty placaProperty()               { return placa; }
    public StringProperty subservicioAfectadoProperty() { return subservicioAfectado; }
    public StringProperty tipoDefectoProperty()         { return tipoDefecto; }
    public StringProperty descripcionProperty()         { return descripcion; }
    public StringProperty fechaDeteccionProperty()      { return fechaDeteccion; }
    public StringProperty supervisorProperty()          { return supervisor; }
    public StringProperty estadoProperty()              { return estado; }
}