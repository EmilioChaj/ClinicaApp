package com.gestioncitas.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gestioncitas.app.R;
import com.gestioncitas.app.dao.PacienteDao;
import com.gestioncitas.app.model.Paciente;
import com.gestioncitas.app.session.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

/** Pantalla de registro de una nueva cuenta de paciente. */
public class RegistroActivity extends AppCompatActivity {

    private static final int MIN_PASSWORD = 6;

    private TextInputEditText editNombre, editUsuario, editPassword, editConfirmar;
    private TextView textoError;
    private PacienteDao pacienteDao;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        pacienteDao = new PacienteDao(this);
        session = new SessionManager(this);

        editNombre = findViewById(R.id.editNombre);
        editUsuario = findViewById(R.id.editUsuario);
        editPassword = findViewById(R.id.editPassword);
        editConfirmar = findViewById(R.id.editConfirmarPassword);
        textoError = findViewById(R.id.textoError);
        Button botonRegistrar = findViewById(R.id.botonRegistrar);
        TextView linkLogin = findViewById(R.id.linkLogin);

        botonRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentarRegistrar();
            }
        });

        linkLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegistroActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void intentarRegistrar() {
        textoError.setVisibility(View.GONE);

        String nombre = editNombre.getText() == null ? "" : editNombre.getText().toString().trim();
        String usuario = editUsuario.getText() == null ? "" : editUsuario.getText().toString().trim();
        String password = editPassword.getText() == null ? "" : editPassword.getText().toString().trim();
        String confirmar = editConfirmar.getText() == null ? "" : editConfirmar.getText().toString().trim();

        if (nombre.isEmpty() || usuario.isEmpty() || password.isEmpty() || confirmar.isEmpty()) {
            mostrarError(R.string.registro_campos_vacios);
            return;
        }

        if (!password.equals(confirmar)) {
            mostrarError(R.string.registro_passwords_no_coinciden);
            return;
        }

        if (password.length() < MIN_PASSWORD) {
            mostrarError(R.string.registro_password_corta);
            return;
        }

        Paciente paciente = pacienteDao.registrarPaciente(nombre, usuario, password);
        if (paciente == null) {
            mostrarError(R.string.registro_usuario_existe);
            return;
        }

        session.guardarSesion(paciente.getId(), paciente.getNombre(), paciente.getUsuario());
        Toast.makeText(this, "Bienvenido, " + paciente.getNombre(), Toast.LENGTH_SHORT).show();
        irAMedicos();
    }

    private void mostrarError(int idString) {
        textoError.setText(idString);
        textoError.setVisibility(View.VISIBLE);
    }

    private void irAMedicos() {
        Intent intent = new Intent(this, MedicosActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
