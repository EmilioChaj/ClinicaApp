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

/** Pantalla de inicio de sesión del paciente. */
public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editUsuario, editPassword;
    private TextView textoError;
    private PacienteDao pacienteDao;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        pacienteDao = new PacienteDao(this);
        session = new SessionManager(this);

        // Si ya hay sesión iniciada, ir directo a médicos
        if (session.haySesion()) {
            irAMedicos();
            return;
        }

        editUsuario = findViewById(R.id.editUsuario);
        editPassword = findViewById(R.id.editPassword);
        textoError = findViewById(R.id.textoError);
        Button botonLogin = findViewById(R.id.botonLogin);

        botonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentarLogin();
            }
        });
    }

    private void intentarLogin() {
        textoError.setVisibility(View.GONE);

        String usuario = editUsuario.getText() == null ? "" : editUsuario.getText().toString().trim();
        String password = editPassword.getText() == null ? "" : editPassword.getText().toString().trim();

        if (usuario.isEmpty() || password.isEmpty()) {
            textoError.setText(R.string.login_campos_vacios);
            textoError.setVisibility(View.VISIBLE);
            return;
        }

        Paciente paciente = pacienteDao.validarLogin(usuario, password);
        if (paciente != null) {
            session.guardarSesion(paciente.getId(), paciente.getNombre(), paciente.getUsuario());
            Toast.makeText(this, "Bienvenido, " + paciente.getNombre(), Toast.LENGTH_SHORT).show();
            irAMedicos();
        } else {
            textoError.setText(R.string.login_credenciales_incorrectas);
            textoError.setVisibility(View.VISIBLE);
        }
    }

    private void irAMedicos() {
        Intent intent = new Intent(this, MedicosActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
