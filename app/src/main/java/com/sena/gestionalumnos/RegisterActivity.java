package com.sena.gestionalumnos;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

/**
 * Pantalla 2: Registro de usuario.
 * Crea una cuenta nueva en Firebase Authentication.
 */
public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText editTextEmail, editTextPassword, editTextConfirm;
    private Button buttonSignUp;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firebaseAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextConfirm = findViewById(R.id.password_confirm);
        buttonSignUp = findViewById(R.id.sign_up_button);
        progressBar = findViewById(R.id.progress);
        TextView signInLink = findViewById(R.id.sign_in_link);

        // Volver a la pantalla de inicio de sesión
        signInLink.setOnClickListener(v -> irAlLogin());

        // Registrar usuario
        buttonSignUp.setOnClickListener(v -> registrarUsuario());
    }

    private void registrarUsuario() {
        String email = String.valueOf(editTextEmail.getText()).trim();
        String password = String.valueOf(editTextPassword.getText()).trim();
        String confirm = String.valueOf(editTextConfirm.getText()).trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Ingrese su correo electrónico", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "El correo no es válido", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Ingrese una contraseña", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener mínimo 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirm)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        mostrarCargando(true);
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    mostrarCargando(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                        // Firebase inicia sesión al registrar; se cierra para que el usuario ingrese en el login
                        firebaseAuth.signOut();
                        irAlLogin();
                    } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(RegisterActivity.this, "Ese correo ya está registrado", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registro fallido, intente de nuevo", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void irAlLogin() {
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
        finish();
    }

    private void mostrarCargando(boolean cargando) {
        progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        buttonSignUp.setEnabled(!cargando);
    }
}
