package com.example.puestosypintado.modelo;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;

public class SubservicioPintura {
    private final StringProperty pieza;
    private final StringProperty servicios;
    private final DoubleProperty costoTotal;

    public SubservicioPintura(String pieza, String servicios, double costoTotal) {
        this.pieza = new SimpleStringProperty(pieza);
        this.servicios = new SimpleStringProperty(servicios);
        this.costoTotal = new SimpleDoubleProperty(costoTotal);
    }

    public String getPieza() { return pieza.get(); }
    public String getServicios() { return servicios.get(); }
    public double getCostoTotal() { return costoTotal.get(); }

    public StringProperty piezaProperty() { return pieza; }
    public StringProperty serviciosProperty() { return servicios; }
    public DoubleProperty costoTotalProperty() { return costoTotal; }
}