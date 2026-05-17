package com.example.puestosypintado.modelo;

import javafx.beans.property.*;

public class Empleado {
    private final StringProperty nombre_completo  = new SimpleStringProperty();
    private final StringProperty puesto  = new SimpleStringProperty();
    private final StringProperty telefono  = new SimpleStringProperty();

    // Solo los campos que se muestran en la tabla por rendimiento visual
    public Empleado(String nombre, String puesto, String telefono) {
        this.nombre_completo.set(nombre);
        this.puesto.set(puesto);
        this.telefono.set(telefono);
    }

    public String getNombre_completo() {
        return nombre_completo.get();
    }

    public StringProperty nombre_completoProperty() {
        return nombre_completo;
    }

    public String getNombre() { return nombre_completo.get(); }
    public String getPuesto() { return puesto.get(); }
    public String getTelefono() { return telefono.get(); }

    public StringProperty nombreProperty() { return nombre_completo; }
    public StringProperty puestoProperty() { return puesto; }
    public StringProperty telefonoProperty() { return telefono; }
}