package com.example.puestosypintado.modelo;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

public class OrdenTrabajo {
    private final IntegerProperty id;
    private final StringProperty vehiculo;
    private final StringProperty estado;

    public OrdenTrabajo(int ot, String vehiculo, String estado) {
        this.id = new SimpleIntegerProperty(ot);
        this.vehiculo = new SimpleStringProperty(vehiculo);
        this.estado = new SimpleStringProperty(estado);
    }

    public int getOt() { return id.get(); }
    public String getVehiculo() { return vehiculo.get(); }
    public String getEstado() { return estado.get(); }

    public void setId(int id) { this.setId(id); }
    public void setVehiculo(String vehiculo) { this.vehiculo.set(vehiculo); }
    public void setEstado(String estado) { this.estado.set(estado); }

    public IntegerProperty idProperty() { return id; }
    public StringProperty vehiculoProperty() { return vehiculo; }
    public StringProperty estadoProperty() { return estado; }
}