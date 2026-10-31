package com.gestioncitas.app.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.gestioncitas.app.db.DatabaseHelper;
import com.gestioncitas.app.model.Paciente;
import com.gestioncitas.app.util.PasswordUtil;

/** Acceso a datos de los pacientes (login). */
public class PacienteDao {

    private final DatabaseHelper helper;

    public PacienteDao(Context context) {
        helper = DatabaseHelper.getInstance(context);
    }

    /**
     * Valida usuario y contraseña. Devuelve el paciente o null si son incorrectos.
     * La contraseña guardada en SQLite es "sal:hash" (SHA-256), así que se busca
     * por usuario y se compara re-hasheando con la sal almacenada.
     */
    public Paciente validarLogin(String usuario, String password) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Paciente paciente = null;
        Cursor c = db.query(DatabaseHelper.TABLE_PACIENTES,
                null,
                DatabaseHelper.COL_USUARIO + "=?",
                new String[]{usuario},
                null, null, null);
        if (c.moveToFirst()) {
            String guardado = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PASSWORD));
            if (PasswordUtil.verificar(password, guardado)) {
                paciente = new Paciente(
                        c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_ID)),
                        c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_NOMBRE)),
                        c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO)),
                        guardado);
            }
        }
        c.close();
        return paciente;
    }

    /**
     * Registra un nuevo paciente. Devuelve el paciente creado con su id,
     * o null si el usuario ya existe (columna UNIQUE de la tabla pacientes).
     * La contraseña se guarda hasheada ("sal:hash").
     */
    public Paciente registrarPaciente(String nombre, String usuario, String password) {
        SQLiteDatabase db = helper.getWritableDatabase();

        // Verificar usuario duplicado antes de insertar
        Cursor c = db.query(DatabaseHelper.TABLE_PACIENTES,
                new String[]{DatabaseHelper.COL_ID},
                DatabaseHelper.COL_USUARIO + "=?",
                new String[]{usuario},
                null, null, null);
        boolean existe = c.moveToFirst();
        c.close();
        if (existe) {
            return null;
        }

        ContentValues valores = new ContentValues();
        valores.put(DatabaseHelper.COL_NOMBRE, nombre);
        valores.put(DatabaseHelper.COL_USUARIO, usuario);
        valores.put(DatabaseHelper.COL_PASSWORD, PasswordUtil.hash(password));

        long nuevoId = db.insert(DatabaseHelper.TABLE_PACIENTES, null, valores);
        if (nuevoId == -1) {
            return null;
        }
        return new Paciente((int) nuevoId, nombre, usuario, null);
    }
}
