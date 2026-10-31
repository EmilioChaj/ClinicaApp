package com.gestioncitas.app.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gestioncitas.app.R;
import com.gestioncitas.app.adapter.CitaAdapter;
import com.gestioncitas.app.dao.CitaDao;
import com.gestioncitas.app.model.Cita;
import com.gestioncitas.app.session.SessionManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Muestra las citas del paciente en sesión.
 * Pestaña "Próximas": citas futuras programadas (se pueden cancelar).
 * Pestaña "Historial": citas pasadas o canceladas.
 */
public class MisCitasActivity extends AppCompatActivity {

    private CitaDao citaDao;
    private SessionManager session;

    private ListView listaCitas;
    private TextView textoVacio;
    private Button botonProximas, botonHistorial;

    private List<Cita> todasLasCitas = new ArrayList<>();
    private boolean mostrandoProximas = true;
    private CitaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_citas);

        citaDao = new CitaDao(this);
        session = new SessionManager(this);

        if (!session.haySesion()) {
            finish();
            return;
        }

        setTitle(R.string.mis_citas_title);

        listaCitas = findViewById(R.id.listaCitas);
        textoVacio = findViewById(R.id.textoVacio);
        botonProximas = findViewById(R.id.botonProximas);
        botonHistorial = findViewById(R.id.botonHistorial);

        adapter = new CitaAdapter(this, new ArrayList<Cita>(), true,
                new CitaAdapter.OnCancelarClickListener() {
                    @Override
                    public void onCancelarClick(Cita cita) {
                        confirmarCancelacion(cita);
                    }
                });
        listaCitas.setAdapter(adapter);

        botonProximas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrandoProximas = true;
                refrescar();
            }
        });

        botonHistorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrandoProximas = false;
                refrescar();
            }
        });

        refrescar();
    }

    /** Recarga la lista desde SQLite y aplica el filtro de la pestaña activa. */
    private void refrescar() {
        todasLasCitas = citaDao.obtenerPorPaciente(session.getId());
        List<Cita> filtradas = new ArrayList<>();
        LocalDate hoy = LocalDate.now();

        for (Cita cita : todasLasCitas) {
            LocalDate fechaCita;
            try {
                fechaCita = LocalDate.parse(cita.getFecha());
            } catch (Exception e) {
                continue;
            }
            boolean futura = !fechaCita.isBefore(hoy);
            if (mostrandoProximas) {
                if (cita.esProgramada() && futura) {
                    filtradas.add(cita);
                }
            } else {
                if (cita.esCancelada() || !futura) {
                    filtradas.add(cita);
                }
            }
        }

        adapter.clear();
        adapter.addAll(filtradas);
        adapter.notifyDataSetChanged();

        if (filtradas.isEmpty()) {
            textoVacio.setVisibility(View.VISIBLE);
            listaCitas.setVisibility(View.GONE);
        } else {
            textoVacio.setVisibility(View.GONE);
            listaCitas.setVisibility(View.VISIBLE);
        }

        // Indicar visualmente la pestaña activa con la opacidad
        botonProximas.setAlpha(mostrandoProximas ? 1.0f : 0.5f);
        botonHistorial.setAlpha(mostrandoProximas ? 0.5f : 1.0f);
    }

    /** Diálogo de confirmación antes de cancelar la cita. */
    private void confirmarCancelacion(final Cita cita) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.cancelar_cita)
                .setMessage(R.string.cancelar_confirmacion)
                .setPositiveButton(R.string.cancelar_si, (dialog, which) -> {
                    boolean ok = citaDao.cancelarCita(cita.getId(), session.getId());
                    if (ok) {
                        Toast.makeText(this, R.string.cancelar_exito, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, R.string.cancelar_solo_futuras, Toast.LENGTH_SHORT).show();
                    }
                    refrescar();
                })
                .setNegativeButton(R.string.cancelar_no, null)
                .show();
    }
}
