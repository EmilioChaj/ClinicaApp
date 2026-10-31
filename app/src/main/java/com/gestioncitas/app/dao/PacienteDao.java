package com.gestioncitas.app.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.gestioncitas.app.db.DatabaseHelper;
import com.gestioncitas.app.model.Paciente;

/** Acceso a datos de los pacientes (login). */
public class PacienteDao {

    private final DatabaseHelper helper;

    public PacienteDao(Context context) {
        helper = DatabaseHelper.getInstance(context);
    }

    /** Valida usuario y contraseña. Devuelve el paciente o null si son incorrectos. */
    public Paciente validarLogin(String usuario, String password) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Paciente paciente = null;
        Cursor c = db.query(DatabaseHelper.TABLE_PACIENTES,
                null,
                DatabaseHelper.COL_USUARIO + "=? AND " + DatabaseHelper.COL_PASSWORD + "=?",
                new String[]{usuario, password},
                null, null, null);
        if (c.moveToFirst()) {
            paciente = new Paciente(
                    c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_ID)),
                    c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_NOMBRE)),
                    c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO)),
                    c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PASSWORD)));
        }
        c.close();
        return paciente;
    }
}
