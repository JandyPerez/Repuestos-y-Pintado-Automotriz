package com.example.puestosypintado.modelo;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Usuario {
    private final StringProperty id_usuario;
    private final StringProperty nombre_usuario; // El "username"
    private final StringProperty rol;
    private final StringProperty estado;

    // Constructor solo con los campos que se muestran en la tabla
    public Usuario(String id, String usuario, String rol, String estado) {
        this.id_usuario = new SimpleStringProperty(id);
        this.nombre_usuario = new SimpleStringProperty(usuario);
        this.rol = new SimpleStringProperty(rol);
        this.estado = new SimpleStringProperty(estado);
    }

    public StringProperty idProperty() { return id_usuario; }
    public StringProperty usuarioProperty() { return nombre_usuario; }
    public StringProperty rolProperty() { return rol; }
    public StringProperty estadoProperty() { return estado; }
}