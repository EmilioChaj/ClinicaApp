package com.gestioncitas.app.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.gestioncitas.app.db.DatabaseHelper;
import com.gestioncitas.app.model.Cita;

import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de las citas.
 * Aplica las reglas del negocio: el horario debe estar libre al confirmar
 * y cada paciente solo consulta/administra sus propias citas.
 */
public class CitaDao {

    private final DatabaseHelper helper;

    public CitaDao(Context context) {
        helper = DatabaseHelper.getInstance(context);
    }

    /** Indica si ya existe una cita programada para ese médico, fecha y hora. */
    public boolean horarioOcupado(int medicoId, String fecha, String hora) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CITAS +
                " WHERE " + DatabaseHelper.COL_MEDICO_ID + "=? AND " +
                DatabaseHelper.COL_FECHA + "=? AND " + DatabaseHelper.COL_HORA + "=? AND " +
                DatabaseHelper.COL_ESTADO + "=?",
                new String[]{String.valueOf(medicoId), fecha, hora, Cita.ESTADO_PROGRAMADA});
        boolean ocupado = false;
        if (c.moveToFirst()) {
            ocupado = c.getInt(0) > 0;
        }
        c.close();
        return ocupado;
    }

    /**
     * Intenta registrar una cita. Devuelve true si se guardó.
     * Revalida en la base de datos que el horario siga libre (evita doble reserva).
     */
    public boolean programarCita(Cita cita) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            // Revalidación de unicidad dentro de la transacción
            Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CITAS +
                    " WHERE " + DatabaseHelper.COL_MEDICO_ID + "=? AND " +
                    DatabaseHelper.COL_FECHA + "=? AND " + DatabaseHelper.COL_HORA + "=? AND " +
                    DatabaseHelper.COL_ESTADO + "=?",
                    new String[]{String.valueOf(cita.getMedicoId()), cita.getFecha(), cita.getHora(),
                            Cita.ESTADO_PROGRAMADA});
            boolean ocupado = false;
            if (c.moveToFirst()) {
                ocupado = c.getInt(0) > 0;
            }
            c.close();
            if (ocupado) {
                return false;
            }

            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_PACIENTE_ID, cita.getPacienteId());
            values.put(DatabaseHelper.COL_MEDICO_ID, cita.getMedicoId());
            values.put(DatabaseHelper.COL_FECHA, cita.getFecha());
            values.put(DatabaseHelper.COL_HORA, cita.getHora());
            values.put(DatabaseHelper.COL_MOTIVO, cita.getMotivo());
            values.put(DatabaseHelper.COL_ESTADO, cita.getEstado());

            long id = db.insert(DatabaseHelper.TABLE_CITAS, null, values);
            if (id > 0) {
                cita.setId((int) id);
                db.setTransactionSuccessful();
                return true;
            }
            return false;
        } finally {
            db.endTransaction();
        }
    }

    /** Cancela una cita (solo si es del paciente indicado y está programada). */
    public boolean cancelarCita(int citaId, int pacienteId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_ESTADO, Cita.ESTADO_CANCELADA);
        int filas = db.update(DatabaseHelper.TABLE_CITAS, values,
                DatabaseHelper.COL_ID + "=? AND " + DatabaseHelper.COL_PACIENTE_ID + "=? AND " +
                        DatabaseHelper.COL_ESTADO + "=?",
                new String[]{String.valueOf(citaId), String.valueOf(pacienteId), Cita.ESTADO_PROGRAMADA});
        return filas > 0;
    }

    /** Devuelve las citas de un paciente con la información del médico (para la lista). */
    public List<Cita> obtenerPorPaciente(int pacienteId) {
        List<Cita> citas = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT c." + DatabaseHelper.COL_ID + ", c." + DatabaseHelper.COL_PACIENTE_ID +
                ", c." + DatabaseHelper.COL_MEDICO_ID + ", c." + DatabaseHelper.COL_FECHA +
                ", c." + DatabaseHelper.COL_HORA + ", c." + DatabaseHelper.COL_MOTIVO +
                ", c." + DatabaseHelper.COL_ESTADO +
                ", m." + DatabaseHelper.COL_NOMBRE + ", m." + DatabaseHelper.COL_ESPECIALIDAD +
                " FROM " + DatabaseHelper.TABLE_CITAS + " c" +
                " INNER JOIN " + DatabaseHelper.TABLE_MEDICOS + " m" +
                " ON c." + DatabaseHelper.COL_MEDICO_ID + " = m." + DatabaseHelper.COL_ID +
                " WHERE c." + DatabaseHelper.COL_PACIENTE_ID + "=?" +
                " ORDER BY c." + DatabaseHelper.COL_FECHA + " DESC, c." + DatabaseHelper.COL_HORA + " DESC";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(pacienteId)});
        while (c.moveToNext()) {
            Cita cita = new Cita(
                    c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_ID)),
                    c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_PACIENTE_ID)),
                    c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_MEDICO_ID)),
                    c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_FECHA)),
                    c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_HORA)),
                    c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_MOTIVO)),
                    c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ESTADO)));
            cita.setMedicoNombre(c.getString(c.getColumnIndexOrThrow("nombre")));
            cita.setEspecialidad(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ESPECIALIDAD)));
            citas.add(cita);
        }
        c.close();
        return citas;
    }
}
