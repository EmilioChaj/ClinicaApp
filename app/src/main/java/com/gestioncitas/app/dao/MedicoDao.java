package com.gestioncitas.app.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.gestioncitas.app.db.DatabaseHelper;
import com.gestioncitas.app.model.Medico;

import java.util.ArrayList;
import java.util.List;

/** Acceso a datos de los médicos. */
public class MedicoDao {

    private final DatabaseHelper helper;

    public MedicoDao(Context context) {
        helper = DatabaseHelper.getInstance(context);
    }

    /** Devuelve todos los médicos disponibles. */
    public List<Medico> obtenerTodos() {
        List<Medico> medicos = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT " + DatabaseHelper.COL_ID + "," +
                DatabaseHelper.COL_NOMBRE + "," + DatabaseHelper.COL_ESPECIALIDAD +
                " FROM " + DatabaseHelper.TABLE_MEDICOS +
                " ORDER BY " + DatabaseHelper.COL_ESPECIALIDAD, null);
        while (c.moveToNext()) {
            medicos.add(new Medico(
                    c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_ID)),
                    c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_NOMBRE)),
                    c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ESPECIALIDAD))));
        }
        c.close();
        return medicos;
    }

    /** Busca un médico por su id. */
    public Medico obtenerPorId(int medicoId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Medico medico = null;
        Cursor c = db.rawQuery("SELECT " + DatabaseHelper.COL_ID + "," +
                DatabaseHelper.COL_NOMBRE + "," + DatabaseHelper.COL_ESPECIALIDAD +
                " FROM " + DatabaseHelper.TABLE_MEDICOS +
                " WHERE " + DatabaseHelper.COL_ID + "=?", new String[]{String.valueOf(medicoId)});
        if (c.moveToFirst()) {
            medico = new Medico(
                    c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_ID)),
                    c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_NOMBRE)),
                    c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ESPECIALIDAD)));
        }
        c.close();
        return medico;
    }
}
