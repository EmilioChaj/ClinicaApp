package com.gestioncitas.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.gestioncitas.app.R;
import com.gestioncitas.app.adapter.MedicoAdapter;
import com.gestioncitas.app.dao.MedicoDao;
import com.gestioncitas.app.model.Medico;
import com.gestioncitas.app.session.SessionManager;

import java.util.List;

/** Lista de médicos disponibles. Al tocar uno se consulta su disponibilidad. */
public class MedicosActivity extends AppCompatActivity {

    private MedicoDao medicoDao;
    private SessionManager session;
    private List<Medico> medicos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicos);

        medicoDao = new MedicoDao(this);
        session = new SessionManager(this);

        if (!session.haySesion()) {
            irALogin();
            return;
        }

        setTitle(getString(R.string.medicos_title) + " — " + session.getNombre());

        ListView lista = findViewById(R.id.listaMedicos);
        TextView vacio = findViewById(R.id.textoVacio);

        medicos = medicoDao.obtenerTodos();
        if (medicos.isEmpty()) {
            vacio.setVisibility(View.VISIBLE);
            return;
        }

        MedicoAdapter adapter = new MedicoAdapter(this, medicos);
        lista.setAdapter(adapter);
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Medico medico = medicos.get(position);
                Intent intent = new Intent(MedicosActivity.this, DisponibilidadActivity.class);
                intent.putExtra("medico_id", medico.getId());
                intent.putExtra("medico_nombre", medico.getNombre());
                intent.putExtra("medico_especialidad", medico.getEspecialidad());
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_medicos, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_mis_citas) {
            startActivity(new Intent(this, MisCitasActivity.class));
            return true;
        } else if (id == R.id.action_cerrar_sesion) {
            session.cerrarSesion();
            Toast.makeText(this, R.string.sesion_cerrada, Toast.LENGTH_SHORT).show();
            irALogin();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void irALogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
