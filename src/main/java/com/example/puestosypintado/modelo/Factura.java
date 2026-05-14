package com.example.puestosypintado.modelo;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

public class Factura {
    private final StringProperty numFac;
    private final StringProperty clienteFac;
    private final StringProperty estadoFac;

    public Factura(String numFac, String clienteFac, String estadoFac) {
        this.numFac = new SimpleStringProperty(numFac);
        this.clienteFac = new SimpleStringProperty(clienteFac);
        this.estadoFac = new SimpleStringProperty(estadoFac);
    }

    public String getNumFac() { return numFac.get(); }
    public String getClienteFac() { return clienteFac.get(); }
    public String getEstadoFac() { return estadoFac.get(); }

    public void setNumFac(String numFac) { this.numFac.set(numFac); }
    public void setClienteFac(String clienteFac) { this.clienteFac.set(clienteFac); }
    public void setEstadoFac(String estadoFac) { this.estadoFac.set(estadoFac); }

    public StringProperty numFacProperty() { return numFac; }
    public StringProperty clienteFacProperty() { return clienteFac; }
    public StringProperty estadoFacProperty() { return estadoFac; }
}