package com.example.puestosypintado.modelo;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Cliente {

    private final StringProperty id_cliente;
    private final StringProperty nombre;
    private final StringProperty cedula;
    private final StringProperty telefono;

    // 🔹 Constructor completo (el que más vas a usar)
    public Cliente(String id_cliente, String nombre, String cedula, String telefono) {
        this.id_cliente = new SimpleStringProperty(id_cliente);
        this.nombre = new SimpleStringProperty(nombre);
        this.cedula = new SimpleStringProperty(cedula);
        this.telefono = new SimpleStringProperty(telefono);
    }

    // 🔹 Constructor simple (para compatibilidad con tu tabla actual)
    public Cliente(String cedula, String nombre) {
        this.id_cliente = new SimpleStringProperty("");
        this.nombre = new SimpleStringProperty(nombre);
        this.cedula = new SimpleStringProperty(cedula);
        this.telefono = new SimpleStringProperty("");
    }

    // ═══════════════════════════════
    // GETTERS
    // ═══════════════════════════════
    public String getId_cliente() { return id_cliente.get(); }
    public String getNombre() { return nombre.get(); }
    public String getCedula() { return cedula.get(); }
    public String getTelefono() { return telefono.get(); }

    // ═══════════════════════════════
    // SETTERS
    // ═══════════════════════════════
    public void setId_cliente(String id_cliente) { this.id_cliente.set(id_cliente); }
    public void setNombre(String nombre) { this.nombre.set(nombre); }
    public void setCedula(String cedula) { this.cedula.set(cedula); }
    public void setTelefono(String telefono) { this.telefono.set(telefono); }

    // ═══════════════════════════════
    // PROPERTIES (para JavaFX TableView)
    // ═══════════════════════════════
    public StringProperty id_clienteProperty() { return id_cliente; }
    public StringProperty nombreProperty() { return nombre; }
    public StringProperty cedulaProperty() { return cedula; }
    public StringProperty telefonoProperty() { return telefono; }
}