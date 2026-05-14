package com.example.puestosypintado.util;

/**
 * Clase estática que mantiene el estado de la sesión activa.
 * Usada para pasar datos entre controladores sin inyección de dependencias.
 */
public class Sesion {

    // ─── Usuario activo ───────────────────────────────────────────
    public static int    idUsuario        = 0;
    public static String nombreUsuario    = "";
    public static String rolUsuario       = "";

    // ─── Servicio de pintura activo ───────────────────────────────
    public static int    idServicioPintura = 0;
    public static String estadoServicio    = "";

    // ─── Incidencia activa ────────────────────────────────────────
    public static int    idIncidencia      = 0;

    // ─── Limpia solo datos de navegación (no el usuario) ─────────
    public static void limpiarNavegacion() {
        idServicioPintura = 0;
        estadoServicio    = "";
        idIncidencia      = 0;
    }
}