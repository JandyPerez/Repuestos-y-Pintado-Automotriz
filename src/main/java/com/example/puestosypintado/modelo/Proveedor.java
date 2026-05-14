package com.example.puestosypintado.modelo;

import javafx.beans.property.*;

/**
 * Modelo completo del Proveedor.
 * Contiene todas las columnas de la tabla Proveedor.
 */
public class Proveedor {

    // ─── Propiedades (nombres de columna reales en la BD) ────────
    private final IntegerProperty idProveedor       = new SimpleIntegerProperty();
    private final StringProperty  rncNit            = new SimpleStringProperty();
    private final StringProperty  nombreEmpresa     = new SimpleStringProperty();
    private final StringProperty  telefonoEmpresa   = new SimpleStringProperty();
    private final StringProperty  correoEmpresa     = new SimpleStringProperty();
    private final StringProperty  nombreContacto    = new SimpleStringProperty();
    private final StringProperty  telefonoContacto  = new SimpleStringProperty();
    private final StringProperty  categoria         = new SimpleStringProperty();
    private final StringProperty  notas             = new SimpleStringProperty();
    private final StringProperty  estado            = new SimpleStringProperty();
    private final StringProperty  direccion         = new SimpleStringProperty();
    private final StringProperty  sector            = new SimpleStringProperty();
    private final StringProperty  ciudad            = new SimpleStringProperty();

    // ─── Constructor para TableView (SELECT lista) ───────────────
    public Proveedor(String id, String empresa, String contacto, String categoria) {
        this.idProveedor.set(id == null ? 0 : Integer.parseInt(id));
        this.nombreEmpresa.set(empresa);
        this.nombreContacto.set(contacto);
        this.categoria.set(categoria);
    }

    // ─── Constructor completo (para búsqueda y formulario) ───────
    public Proveedor(int idProveedor, String rncNit, String nombreEmpresa,
                     String telefonoEmpresa, String correoEmpresa,
                     String nombreContacto, String telefonoContacto,
                     String categoria, String notas, String estado,
                     String direccion, String sector, String ciudad) {

        this.idProveedor.set(idProveedor);
        this.rncNit.set(rncNit);
        this.nombreEmpresa.set(nombreEmpresa);
        this.telefonoEmpresa.set(telefonoEmpresa);
        this.correoEmpresa.set(correoEmpresa);
        this.nombreContacto.set(nombreContacto);
        this.telefonoContacto.set(telefonoContacto);
        this.categoria.set(categoria);
        this.notas.set(notas);
        this.estado.set(estado);
        this.direccion.set(direccion);
        this.sector.set(sector);
        this.ciudad.set(ciudad);
    }

    // ─── Getters ─────────────────────────────────────────────────
    public int    getIdProveedor()      { return idProveedor.get(); }
    public String getRncNit()           { return rncNit.get(); }
    public String getNombreEmpresa()    { return nombreEmpresa.get(); }
    public String getTelefonoEmpresa()  { return telefonoEmpresa.get(); }
    public String getCorreoEmpresa()    { return correoEmpresa.get(); }
    public String getNombreContacto()   { return nombreContacto.get(); }
    public String getTelefonoContacto() { return telefonoContacto.get(); }
    public String getCategoria()        { return categoria.get(); }
    public String getNotas()            { return notas.get(); }
    public String getEstado()           { return estado.get(); }
    public String getDireccion()        { return direccion.get(); }
    public String getSector()           { return sector.get(); }
    public String getCiudad()           { return ciudad.get(); }

    // ─── Setters ─────────────────────────────────────────────────
    public void setIdProveedor     (int    v) { idProveedor.set(v); }
    public void setRncNit          (String v) { rncNit.set(v); }
    public void setNombreEmpresa   (String v) { nombreEmpresa.set(v); }
    public void setTelefonoEmpresa (String v) { telefonoEmpresa.set(v); }
    public void setCorreoEmpresa   (String v) { correoEmpresa.set(v); }
    public void setNombreContacto  (String v) { nombreContacto.set(v); }
    public void setTelefonoContacto(String v) { telefonoContacto.set(v); }
    public void setCategoria       (String v) { categoria.set(v); }
    public void setNotas           (String v) { notas.set(v); }
    public void setEstado          (String v) { estado.set(v); }
    public void setDireccion       (String v) { direccion.set(v); }
    public void setSector          (String v) { sector.set(v); }
    public void setCiudad          (String v) { ciudad.set(v); }

    // ─── Properties (para TableView / Binding) ───────────────────
    public IntegerProperty idProveedorProperty()      { return idProveedor; }
    public StringProperty  rncNitProperty()           { return rncNit; }
    public StringProperty  nombreEmpresaProperty()    { return nombreEmpresa; }
    public StringProperty  telefonoEmpresaProperty()  { return telefonoEmpresa; }
    public StringProperty  correoEmpresaProperty()    { return correoEmpresa; }
    public StringProperty  nombreContactoProperty()   { return nombreContacto; }
    public StringProperty  telefonoContactoProperty() { return telefonoContacto; }
    public StringProperty  categoriaProperty()        { return categoria; }
    public StringProperty  notasProperty()            { return notas; }
    public StringProperty  estadoProperty()           { return estado; }
    public StringProperty  direccionProperty()        { return direccion; }
    public StringProperty  sectorProperty()           { return sector; }
    public StringProperty  ciudadProperty()           { return ciudad; }

    // Alias para compatibilidad con el TableView del controlador
    public StringProperty idProperty() {
        return new SimpleStringProperty(String.valueOf(idProveedor.get()));
    }    public StringProperty  empresaProperty()  { return nombreEmpresa; }
    public StringProperty  contactoProperty() { return nombreContacto; }
}