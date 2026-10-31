package com.gestioncitas.app.dao;

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
}
