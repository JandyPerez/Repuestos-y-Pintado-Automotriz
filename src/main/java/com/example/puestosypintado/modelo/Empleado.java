package com.example.puestosypintado.modelo;

import javafx.beans.property.*;

public class Empleado {
    private final StringProperty nombre_completo;
    private final StringProperty puesto;
    private final StringProperty telefono;

    // Solo los campos que se muestran en la tabla por rendimiento visual
    public Empleado(String nombre, String puesto, String telefono) {
        this.nombre_completo = new SimpleStringProperty(nombre);
        this.puesto = new SimpleStringProperty(puesto);
        this.telefono = new SimpleStringProperty(telefono);
    }

    public String getNombre() { return nombre_completo.get(); }
    public String getPuesto() { return puesto.get(); }
    public String getTelefono() { return telefono.get(); }

    public StringProperty nombreProperty() { return nombre_completo; }
    public StringProperty puestoProperty() { return puesto; }
    public StringProperty telefonoProperty() { return telefono; }
}