package com.example.puestosypintado.modelo;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Usuario {
    private final IntegerProperty id_usuario = new SimpleIntegerProperty();
    private final StringProperty nombre_usuario = new SimpleStringProperty(); // El "username"
    private final StringProperty rol = new SimpleStringProperty();
    private final StringProperty estado = new SimpleStringProperty();

    public int getId_usuario() {
        return id_usuario.get();
    }

    public IntegerProperty id_usuarioProperty() {
        return id_usuario;
    }

    public String getNombre_usuario() {
        return nombre_usuario.get();
    }

    public StringProperty nombre_usuarioProperty() {
        return nombre_usuario;
    }

    public String getRol() {
        return rol.get();
    }

    public String getEstado() {
        return estado.get();
    }

    // Constructor soloa con los campos que se muestran en la tabla
    public Usuario(String id, String usuario, String rol, String estado) {
        this.id_usuario.set(Integer.parseInt(id));
        this.nombre_usuario.set(usuario);
        this.rol.set(rol);
        this.estado.set(estado);
    }

    public IntegerProperty idProperty() { return id_usuario; }
    public StringProperty usuarioProperty() { return nombre_usuario; }
    public StringProperty rolProperty() { return rol; }
    public StringProperty estadoProperty() { return estado; }
}