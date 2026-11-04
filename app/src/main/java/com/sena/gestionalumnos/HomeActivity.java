package com.sena.gestionalumnos;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

/**
 * Pantalla 3: Inicio.
 * Permite registrar alumnos y ver el listado guardado en Cloud Firestore.
 */
public class HomeActivity extends AppCompatActivity {

    private static final String COLECCION = "alumnos";

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;

    private TextInputEditText editNombre, editDocumento, editPrograma, editSemestre;
    private Button buttonGuardar;
    private LinearLayout listaAlumnos;
    private TextView textSinAlumnos, textTituloLista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Si no hay sesión iniciada, vuelve al login
        FirebaseUser usuario = firebaseAuth.getCurrentUser();
        if (usuario == null) {
            irAlLogin();
            return;
        }

        TextView textUsuario = findViewById(R.id.user_email);
        textUsuario.setText(usuario.getEmail());

        editNombre = findViewById(R.id.nombre);
        editDocumento = findViewById(R.id.documento);
        editPrograma = findViewById(R.id.programa);
        editSemestre = findViewById(R.id.semestre);
        buttonGuardar = findViewById(R.id.guardar);
        listaAlumnos = findViewById(R.id.lista_alumnos);
        textSinAlumnos = findViewById(R.id.sin_alumnos);
        textTituloLista = findViewById(R.id.titulo_lista);
        Button buttonLogout = findViewById(R.id.logout);

        buttonGuardar.setOnClickListener(v -> guardarAlumno());

        buttonLogout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
            irAlLogin();
        });

        escucharAlumnos();
    }

    /** Guarda un alumno nuevo en la colección "alumnos" de Firestore. */
    private void guardarAlumno() {
        String nombre = String.valueOf(editNombre.getText()).trim();
        String documento = String.valueOf(editDocumento.getText()).trim();
        String programa = String.valueOf(editPrograma.getText()).trim();
        String semestreTexto = String.valueOf(editSemestre.getText()).trim();

        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(documento)
                || TextUtils.isEmpty(programa) || TextUtils.isEmpty(semestreTexto)) {
            Toast.makeText(this, "Complete todos los campos del alumno", Toast.LENGTH_SHORT).show();
            return;
        }

        int semestre = Integer.parseInt(semestreTexto);
        if (semestre < 1 || semestre > 12) {
            Toast.makeText(this, "El semestre debe estar entre 1 y 12", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> alumno = new HashMap<>();
        alumno.put("nombre", nombre);
        alumno.put("documento", documento);
        alumno.put("programa", programa);
        alumno.put("semestre", semestre);
        alumno.put("registradoPor", firebaseAuth.getCurrentUser() != null
                ? firebaseAuth.getCurrentUser().getEmail() : "");
        alumno.put("fechaRegistro", FieldValue.serverTimestamp());

        buttonGuardar.setEnabled(false);
        db.collection(COLECCION)
                .add(alumno)
                .addOnSuccessListener(ref -> {
                    buttonGuardar.setEnabled(true);
                    Toast.makeText(this, "Alumno registrado correctamente", Toast.LENGTH_SHORT).show();
                    limpiarFormulario();
                })
                .addOnFailureListener(e -> {
                    buttonGuardar.setEnabled(true);
                    Toast.makeText(this, "No se pudo guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /** Escucha la colección en tiempo real y pinta el listado. */
    private void escucharAlumnos() {
        db.collection(COLECCION)
                .orderBy("fechaRegistro", Query.Direction.DESCENDING)
                .addSnapshotListener(this, (snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error al cargar alumnos: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (snapshots == null) return;

                    listaAlumnos.removeAllViews();
                    LayoutInflater inflater = LayoutInflater.from(this);

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        View item = inflater.inflate(R.layout.item_alumno, listaAlumnos, false);

                        String nombre = doc.getString("nombre");
                        String documento = doc.getString("documento");
                        String programa = doc.getString("programa");
                        Long semestre = doc.getLong("semestre");

                        ((TextView) item.findViewById(R.id.item_nombre)).setText(nombre);
                        ((TextView) item.findViewById(R.id.item_detalle)).setText(
                                "Doc. " + documento + "  ·  Semestre " + (semestre != null ? semestre : "-"));
                        ((TextView) item.findViewById(R.id.item_programa)).setText(programa);

                        ImageButton eliminar = item.findViewById(R.id.item_eliminar);
                        eliminar.setOnClickListener(v -> confirmarEliminar(doc.getId(), nombre));

                        listaAlumnos.addView(item);
                    }

                    int total = snapshots.size();
                    textTituloLista.setText(getString(R.string.alumnos_registrados) + " (" + total + ")");
                    textSinAlumnos.setVisibility(total == 0 ? View.VISIBLE : View.GONE);
                });
    }

    private void confirmarEliminar(String id, String nombre) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar alumno")
                .setMessage("¿Desea eliminar a " + nombre + "?")
                .setPositiveButton("Eliminar", (dialog, which) ->
                        db.collection(COLECCION).document(id).delete()
                                .addOnSuccessListener(x ->
                                        Toast.makeText(this, "Alumno eliminado", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "No se pudo eliminar", Toast.LENGTH_SHORT).show()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void limpiarFormulario() {
        editNombre.setText("");
        editDocumento.setText("");
        editPrograma.setText("");
        editSemestre.setText("");
        editNombre.requestFocus();
    }

    private void irAlLogin() {
        startActivity(new Intent(HomeActivity.this, MainActivity.class));
        finish();
    }
}
