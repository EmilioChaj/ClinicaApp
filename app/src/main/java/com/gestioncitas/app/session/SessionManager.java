package com.gestioncitas.app.session;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Guarda la sesión del paciente en SharedPreferences para que
 * solo se vean y administren sus propias citas.
 */
public class SessionManager {

    private static final String PREF_NAME = "sesion_paciente";
    private static final String KEY_ID = "paciente_id";
    private static final String KEY_NOMBRE = "paciente_nombre";
    private static final String KEY_USUARIO = "paciente_usuario";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void guardarSesion(int id, String nombre, String usuario) {
        prefs.edit()
                .putInt(KEY_ID, id)
                .putString(KEY_NOMBRE, nombre)
                .putString(KEY_USUARIO, usuario)
                .apply();
    }

    public boolean haySesion() {
        return getId() > 0;
    }

    public int getId() {
        return prefs.getInt(KEY_ID, -1);
    }

    public String getNombre() {
        return prefs.getString(KEY_NOMBRE, "");
    }

    public String getUsuario() {
        return prefs.getString(KEY_USUARIO, "");
    }

    public void cerrarSesion() {
        prefs.edit().clear().apply();
    }
}
