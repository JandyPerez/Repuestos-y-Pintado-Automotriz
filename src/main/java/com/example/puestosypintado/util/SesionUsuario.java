package com.example.puestosypintado.util;

/**
 * Singleton liviano que guarda los datos del usuario autenticado
 * durante toda la sesión de la aplicación.
 *
 * Uso:
 *   // Al iniciar sesión:
 *   SesionUsuario.instancia().iniciar("Juan Pérez", "Administrador");
 *
 *   // En cualquier controlador:
 *   String nombre = SesionUsuario.instancia().getNombre();
 *   String rol    = SesionUsuario.instancia().getRol();
 *
 *   // Al cerrar sesión:
 *   SesionUsuario.instancia().cerrar();
 */
public class SesionUsuario {

    // ── Singleton ────────────────────────────────────────────────
    private static final SesionUsuario INSTANCIA = new SesionUsuario();
    private SesionUsuario() {}
    public static SesionUsuario instancia() { return INSTANCIA; }

    // ── Estado ───────────────────────────────────────────────────
    private String nombre = "";
    private String rol    = "";

    // ── API pública ──────────────────────────────────────────────
    public void iniciar(String nombre, String rol) {
        this.nombre = nombre != null ? nombre : "";
        this.rol    = rol    != null ? rol    : "";
    }

    public void cerrar() {
        this.nombre = "";
        this.rol    = "";
    }

    public String getNombre() { return nombre; }
    public String getRol()    { return rol;    }

    /** true si hay una sesión activa (nombre no vacío). */
    public boolean estaActiva() { return !nombre.isEmpty(); }
}
