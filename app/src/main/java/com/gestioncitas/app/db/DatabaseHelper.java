package com.gestioncitas.app.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.gestioncitas.app.util.PasswordUtil;

/**
 * Administrador de la base de datos SQLite local de la aplicación.
 * Crea las tablas y siembra los datos iniciales de ejemplo (pacientes, médicos y horarios).
 *
 * Versiones:
 * v1 — esquema inicial.
 * v2 — contraseñas almacenadas con SHA-256 + sal ("sal:hash"); migración sin borrar datos.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "gestion_citas.db";
    private static final int DATABASE_VERSION = 2;

    // Tablas
    public static final String TABLE_PACIENTES = "pacientes";
    public static final String TABLE_MEDICOS = "medicos";
    public static final String TABLE_HORARIOS = "horarios_atencion";
    public static final String TABLE_CITAS = "citas";

    // Columnas comunes
    public static final String COL_ID = "id";

    // Columnas pacientes
    public static final String COL_NOMBRE = "nombre";
    public static final String COL_USUARIO = "usuario";
    public static final String COL_PASSWORD = "password";

    // Columnas médicos
    public static final String COL_ESPECIALIDAD = "especialidad";

    // Columnas horarios
    public static final String COL_MEDICO_ID = "medico_id";
    public static final String COL_DIA_SEMANA = "dia_semana";
    public static final String COL_HORA_INICIO = "hora_inicio";
    public static final String COL_HORA_FIN = "hora_fin";

    // Columnas citas
    public static final String COL_PACIENTE_ID = "paciente_id";
    public static final String COL_FECHA = "fecha";
    public static final String COL_HORA = "hora";
    public static final String COL_MOTIVO = "motivo";
    public static final String COL_ESTADO = "estado";

    private static DatabaseHelper instancia;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instancia == null) {
            instancia = new DatabaseHelper(context.getApplicationContext());
        }
        return instancia;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PACIENTES + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NOMBRE + " TEXT NOT NULL, " +
                COL_USUARIO + " TEXT NOT NULL UNIQUE, " +
                // Desde v2 guarda "sal:hash" (SHA-256 + sal), no el texto plano
                COL_PASSWORD + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_MEDICOS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NOMBRE + " TEXT NOT NULL, " +
                COL_ESPECIALIDAD + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_HORARIOS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_MEDICO_ID + " INTEGER NOT NULL, " +
                COL_DIA_SEMANA + " INTEGER NOT NULL, " +
                COL_HORA_INICIO + " TEXT NOT NULL, " +
                COL_HORA_FIN + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + COL_MEDICO_ID + ") REFERENCES " + TABLE_MEDICOS + "(" + COL_ID + "))");

        db.execSQL("CREATE TABLE " + TABLE_CITAS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PACIENTE_ID + " INTEGER NOT NULL, " +
                COL_MEDICO_ID + " INTEGER NOT NULL, " +
                COL_FECHA + " TEXT NOT NULL, " +
                COL_HORA + " TEXT NOT NULL, " +
                COL_MOTIVO + " TEXT NOT NULL, " +
                COL_ESTADO + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + COL_PACIENTE_ID + ") REFERENCES " + TABLE_PACIENTES + "(" + COL_ID + "), " +
                "FOREIGN KEY(" + COL_MEDICO_ID + ") REFERENCES " + TABLE_MEDICOS + "(" + COL_ID + "))");

        // Índice para acelerar la búsqueda de horarios ocupados (médico + fecha + hora)
        db.execSQL("CREATE INDEX idx_citas_horario ON " + TABLE_CITAS +
                "(" + COL_MEDICO_ID + ", " + COL_FECHA + ", " + COL_HORA + ")");

        seed(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Migración incremental: nunca se borran tablas ni datos.
        // Cada versión nueva añade su paso aquí (ej: if (oldVersion < 3) { ... }).
        if (oldVersion < 2) {
            migrarPasswordsAHash(db);
        }
    }

    /**
     * v1 → v2: convierte las contraseñas en texto plano a "sal:hash".
     * No toca citas, médicos ni horarios; solo transforma el campo password.
     */
    private void migrarPasswordsAHash(SQLiteDatabase db) {
        Cursor c = db.query(TABLE_PACIENTES,
                new String[]{COL_ID, COL_PASSWORD},
                null, null, null, null, null);
        try {
            while (c.moveToNext()) {
                String id = String.valueOf(c.getInt(c.getColumnIndexOrThrow(COL_ID)));
                String valor = c.getString(c.getColumnIndexOrThrow(COL_PASSWORD));
                if (!PasswordUtil.esHash(valor)) { // aún en texto plano
                    ContentValues cv = new ContentValues();
                    cv.put(COL_PASSWORD, PasswordUtil.hash(valor));
                    db.update(TABLE_PACIENTES, cv, COL_ID + "=?", new String[]{id});
                }
            }
        } finally {
            c.close();
        }
    }

    /** Inserta los datos de ejemplo: pacientes, médicos y horarios de atención. */
    private void seed(SQLiteDatabase db) {
        // --- Pacientes de prueba (contraseña "1234" almacenada hasheada con sal) ---
        String hashMaria = PasswordUtil.hash("1234");
        String hashJuan = PasswordUtil.hash("1234");
        db.execSQL("INSERT INTO " + TABLE_PACIENTES + " (" + COL_NOMBRE + "," + COL_USUARIO + "," + COL_PASSWORD + ") VALUES " +
                "('María García','maria','" + hashMaria + "'), " +
                "('Juan Pérez','juan','" + hashJuan + "')");

        // --- Médicos de ejemplo ---
        db.execSQL("INSERT INTO " + TABLE_MEDICOS + " (" + COL_NOMBRE + "," + COL_ESPECIALIDAD + ") VALUES " +
                "('Dra. Ana López','Medicina General'), " +
                "('Dr. Carlos Méndez','Pediatría'), " +
                "('Dra. Sofía Ramírez','Cardiología'), " +
                "('Dr. Luis Torres','Dermatología')");

        // --- Horarios de atención: lunes(1) a viernes(5), 08:00-12:00 y 14:00-18:00 ---
        // Se insertan bloques por médico y día de la semana.
        for (int medicoId = 1; medicoId <= 4; medicoId++) {
            for (int dia = 1; dia <= 5; dia++) {
                db.execSQL("INSERT INTO " + TABLE_HORARIOS + " (" +
                        COL_MEDICO_ID + "," + COL_DIA_SEMANA + "," + COL_HORA_INICIO + "," + COL_HORA_FIN +
                        ") VALUES (" + medicoId + ", " + dia + ", '08:00', '12:00')");
                db.execSQL("INSERT INTO " + TABLE_HORARIOS + " (" +
                        COL_MEDICO_ID + "," + COL_DIA_SEMANA + "," + COL_HORA_INICIO + "," + COL_HORA_FIN +
                        ") VALUES (" + medicoId + ", " + dia + ", '14:00', '18:00')");
            }
        }
    }
}
