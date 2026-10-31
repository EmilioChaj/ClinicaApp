package com.gestioncitas.app.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gestioncitas.app.R;
import com.gestioncitas.app.adapter.SlotAdapter;
import com.gestioncitas.app.dao.CitaDao;
import com.gestioncitas.app.dao.HorarioDao;
import com.gestioncitas.app.model.Cita;
import com.gestioncitas.app.model.HorarioAtencion;
import com.gestioncitas.app.model.Slot;
import com.gestioncitas.app.session.SessionManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Muestra los días y horarios disponibles del médico seleccionado.
 * Genera slots de 60 minutos desde el horario de atención, marca los ocupados
 * y no permite seleccionar fechas u horarios pasados.
 */
public class DisponibilidadActivity extends AppCompatActivity {

    private static final int DIAS_A_MOSTRAR = 14;
    private static final int DURACION_CITA_MIN = 60;

    private HorarioDao horarioDao;
    private CitaDao citaDao;
    private SessionManager session;

    private int medicoId;
    private String medicoNombre, medicoEspecialidad;

    private Spinner spinnerFechas;
    private ListView listaSlots;
    private TextView textoVacio;

    private final List<LocalDate> fechasDisponibles = new ArrayList<>();
    private List<Slot> slotsDeFecha = new ArrayList<>();
    private SlotAdapter slotAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disponibilidad);

        horarioDao = new HorarioDao(this);
        citaDao = new CitaDao(this);
        session = new SessionManager(this);

        if (!session.haySesion()) {
            finish();
            return;
        }

        medicoId = getIntent().getIntExtra("medico_id", -1);
        medicoNombre = getIntent().getStringExtra("medico_nombre");
        medicoEspecialidad = getIntent().getStringExtra("medico_especialidad");

        TextView textoMedico = findViewById(R.id.textoMedico);
        TextView textoEsp = findViewById(R.id.textoEspecialidad);
        spinnerFechas = findViewById(R.id.spinnerFechas);
        listaSlots = findViewById(R.id.listaSlots);
        textoVacio = findViewById(R.id.textoVacio);

        textoMedico.setText(medicoNombre);
        textoEsp.setText(medicoEspecialidad);
        setTitle(R.string.disponibilidad_title);

        calcularFechasDisponibles();
        configurarSpinner();

        slotAdapter = new SlotAdapter(this, slotsDeFecha);
        listaSlots.setAdapter(slotAdapter);

        listaSlots.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Slot slot = slotsDeFecha.get(position);
                if (slot.isOcupado()) {
                    Toast.makeText(DisponibilidadActivity.this,
                            R.string.programar_error_ocupado, Toast.LENGTH_SHORT).show();
                } else {
                    mostrarDialogoMotivo(slot);
                }
            }
        });
    }

    /** Genera la lista de fechas con al menos un horario de atención en los próximos días. */
    private void calcularFechasDisponibles() {
        fechasDisponibles.clear();
        LocalDate hoy = LocalDate.now();
        for (int i = 0; i < DIAS_A_MOSTRAR; i++) {
            LocalDate fecha = hoy.plusDays(i);
            int diaSemana = fecha.getDayOfWeek().getValue(); // 1 = lunes
            List<HorarioAtencion> bloques = horarioDao.obtenerPorMedicoYDia(medicoId, diaSemana);
            if (!bloques.isEmpty()) {
                fechasDisponibles.add(fecha);
            }
        }
    }

    private void configurarSpinner() {
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("EEEE dd 'de' MMMM", new Locale("es"));
        List<String> etiquetas = new ArrayList<>();
        LocalDate hoy = LocalDate.now();
        for (LocalDate fecha : fechasDisponibles) {
            String etiqueta = fecha.format(formatoFecha);
            if (fecha.equals(hoy)) {
                etiqueta = "Hoy — " + etiqueta;
            }
            etiquetas.add(capitalizar(etiqueta));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, etiquetas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFechas.setAdapter(adapter);

        spinnerFechas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cargarSlots(fechasDisponibles.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /** Genera los slots de 60 min de una fecha y marca los ocupados. */
    private void cargarSlots(LocalDate fecha) {
        slotsDeFecha.clear();
        int diaSemana = fecha.getDayOfWeek().getValue();
        List<HorarioAtencion> bloques = horarioDao.obtenerPorMedicoYDia(medicoId, diaSemana);

        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();
        boolean esHoy = fecha.equals(hoy);

        DateTimeFormatter horaFmt = DateTimeFormatter.ofPattern("HH:mm");

        for (HorarioAtencion bloque : bloques) {
            LocalTime inicio = LocalTime.parse(bloque.getHoraInicio(), horaFmt);
            LocalTime fin = LocalTime.parse(bloque.getHoraFin(), horaFmt);
            LocalTime actual = inicio;
            while (!actual.plusMinutes(DURACION_CITA_MIN).isAfter(fin)) {
                // No mostrar horarios pasados
                boolean pasado = esHoy && actual.isBefore(ahora);
                if (!pasado) {
                    String horaTexto = actual.format(horaFmt);
                    boolean ocupado = citaDao.horarioOcupado(medicoId, fecha.toString(), horaTexto);
                    slotsDeFecha.add(new Slot(fecha.toString(), horaTexto, ocupado));
                }
                actual = actual.plusMinutes(DURACION_CITA_MIN);
            }
        }

        slotAdapter.notifyDataSetChanged();

        if (slotsDeFecha.isEmpty()) {
            textoVacio.setVisibility(View.VISIBLE);
            listaSlots.setVisibility(View.GONE);
        } else {
            textoVacio.setVisibility(View.GONE);
            listaSlots.setVisibility(View.VISIBLE);
        }
    }

    /** Diálogo para ingresar el motivo y confirmar la cita. */
    private void mostrarDialogoMotivo(final Slot slot) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View vista = inflater.inflate(android.R.layout.simple_list_item_1, null);
        final EditText editMotivo = new EditText(this);
        editMotivo.setHint(R.string.programar_hint_motivo);
        editMotivo.setSingleLine(false);
        editMotivo.setMinLines(3);
        editMotivo.setPadding(48, 32, 48, 16);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.programar_title);
        builder.setMessage(slot.getFecha() + "  •  " + slot.getHora());
        builder.setView(editMotivo);
        builder.setPositiveButton(R.string.programar_boton, null);
        builder.setNegativeButton(R.string.programar_cancelar, null);
        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String motivo = editMotivo.getText().toString().trim();
                if (motivo.isEmpty()) {
                    Toast.makeText(DisponibilidadActivity.this,
                            R.string.programar_motivo_vacio, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (guardarCita(slot, motivo)) {
                    dialog.dismiss();
                    Toast.makeText(DisponibilidadActivity.this,
                            R.string.programar_exito, Toast.LENGTH_LONG).show();
                    cargarSlots(LocalDate.parse(slot.getFecha()));
                } else {
                    Toast.makeText(DisponibilidadActivity.this,
                            R.string.programar_error_ocupado, Toast.LENGTH_LONG).show();
                    cargarSlots(LocalDate.parse(slot.getFecha()));
                }
            }
        });
    }

    /** Guarda la cita en SQLite revalidando que el horario siga libre. */
    private boolean guardarCita(Slot slot, String motivo) {
        Cita cita = new Cita();
        cita.setPacienteId(session.getId());
        cita.setMedicoId(medicoId);
        cita.setFecha(slot.getFecha());
        cita.setHora(slot.getHora());
        cita.setMotivo(motivo);
        cita.setEstado(Cita.ESTADO_PROGRAMADA);
        return citaDao.programarCita(cita);
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) return texto;
        return texto.substring(0, 1).toUpperCase(new Locale("es")) + texto.substring(1);
    }
}
