package com.gestioncitas.app.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.gestioncitas.app.db.DatabaseHelper;
import com.gestioncitas.app.model.HorarioAtencion;

import java.util.ArrayList;
import java.util.List;

/** Acceso a datos de los horarios de atención de los médicos. */
public class HorarioDao {

    private final DatabaseHelper helper;

    public HorarioDao(Context context) {
        helper = DatabaseHelper.getInstance(context);
    }

    /** Devuelve los bloques de atención de un médico para un día de la semana (1 = lunes). */
    public List<HorarioAtencion> obtenerPorMedicoYDia(int medicoId, int diaSemana) {
        List<HorarioAtencion> horarios = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT " + DatabaseHelper.COL_ID + "," +
                DatabaseHelper.COL_MEDICO_ID + "," +
                DatabaseHelper.COL_DIA_SEMANA + "," +
                DatabaseHelper.COL_HORA_INICIO + "," +
                DatabaseHelper.COL_HORA_FIN +
                " FROM " + DatabaseHelper.TABLE_HORARIOS +
                " WHERE " + DatabaseHelper.COL_MEDICO_ID + "=? AND " + DatabaseHelper.COL_DIA_SEMANA + "=?" +
                " ORDER BY " + DatabaseHelper.COL_HORA_INICIO,
                new String[]{String.valueOf(medicoId), String.valueOf(diaSemana)});
        while (c.moveToNext()) {
            horarios.add(new HorarioAtencion(
                    c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_ID)),
                    c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_MEDICO_ID)),
                    c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_DIA_SEMANA)),
                    c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_HORA_INICIO)),
                    c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_HORA_FIN))));
        }
        c.close();
        return horarios;
    }
}
