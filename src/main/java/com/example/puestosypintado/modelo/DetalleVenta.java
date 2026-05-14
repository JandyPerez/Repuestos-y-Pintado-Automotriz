package com.example.puestosypintado.modelo;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

public class DetalleVenta {
    private final StringProperty producto;
    private final IntegerProperty cantidad;
    private final DoubleProperty subtotal;

    public DetalleVenta(String producto, int cantidad, double subtotal) {
        this.producto = new SimpleStringProperty(producto);
        this.cantidad = new SimpleIntegerProperty(cantidad);
        this.subtotal = new SimpleDoubleProperty(subtotal);
    }

    public String getProducto() { return producto.get(); }
    public int getCantidad() { return cantidad.get(); }
    public double getSubtotal() { return subtotal.get(); }

    public void setProducto(String producto) { this.producto.set(producto); }
    public void setCantidad(int cantidad) { this.cantidad.set(cantidad); }
    public void setSubtotal(double subtotal) { this.subtotal.set(subtotal); }

    public StringProperty productoProperty() { return producto; }
    public IntegerProperty cantidadProperty() { return cantidad; }
    public DoubleProperty subtotalProperty() { return subtotal; }
}