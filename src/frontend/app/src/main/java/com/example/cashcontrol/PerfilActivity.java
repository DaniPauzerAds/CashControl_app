package com.example.cashcontrol;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class PerfilActivity extends AppCompatActivity {

    TextView tvNomePerfil, tvEmailPerfil;
    Button btnSairConta, btnEditarPerfil;
    int idUsuario;
    String tokenSalvo;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        idUsuario = getIntent().getIntExtra("id_usuario", 0);
        if (idUsuario == 0) {
            SharedPreferences prefs = getSharedPreferences("cashcontrol", MODE_PRIVATE);
            idUsuario = prefs.getInt("id_usuario", 0);
        }

        tvNomePerfil = findViewById(R.id.tvNomePerfil);
        tvEmailPerfil = findViewById(R.id.tvEmailPerfil);
        btnSairConta = findViewById(R.id.btnSairConta);
        btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
        requestQueue = Volley.newRequestQueue(this);

        SharedPreferences prefs = getSharedPreferences("cashcontrol", MODE_PRIVATE);
        String nome = prefs.getString("nome", "");
        String email = prefs.getString("email", "");
        tokenSalvo = prefs.getString("token", "");

        tvNomePerfil.setText(nome);
        tvEmailPerfil.setText(email);

        configurarBottomNav(R.id.nav_perfil);

        btnEditarPerfil.setOnClickListener(v -> mostrarDialogEditar(nome, email));

        btnSairConta.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
            Intent intent = new Intent(PerfilActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void mostrarDialogEditar(String nomeAtual, String emailAtual) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar perfil");

        View view = LayoutInflater.from(this).inflate(R.layout.editar_perfil, null);
        builder.setView(view);

        EditText etNome = view.findViewById(R.id.etEditNome);
        EditText etEmail = view.findViewById(R.id.etEditEmail);
        EditText etSenha = view.findViewById(R.id.etEditSenha);

        etNome.setText(nomeAtual);
        etEmail.setText(emailAtual);

        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String novoNome = etNome.getText().toString().trim();
            String novoEmail = etEmail.getText().toString().trim();
            String novaSenha = etSenha.getText().toString().trim();

            if (novoNome.isEmpty() || novoEmail.isEmpty()) {
                Toast.makeText(this, "Nome e email são obrigatórios!", Toast.LENGTH_SHORT).show();
                return;
            }

            atualizarPerfil(novoNome, novoEmail, novaSenha);
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void atualizarPerfil(String nome, String email, String senha) {
        String url = "https://cashcontrol-app.onrender.com/usuarios/" + idUsuario;

        JSONObject body = new JSONObject();
        try {
            body.put("nome", nome);
            body.put("email", email);
            if (!senha.isEmpty()) {
                body.put("senha", senha);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                body,
                response -> {
                    SharedPreferences prefs = getSharedPreferences("cashcontrol", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("nome", nome);
                    editor.putString("email", email);
                    editor.apply();

                    tvNomePerfil.setText(nome);
                    tvEmailPerfil.setText(email);

                    Toast.makeText(this, "Perfil atualizado!", Toast.LENGTH_SHORT).show();
                },
                error -> Toast.makeText(this, "Erro ao atualizar perfil!", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("authorization", tokenSalvo);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void configurarBottomNav(int itemSelecionado) {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(itemSelecionado);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(PerfilActivity.this, HomeActivity.class);
                intent.putExtra("id_usuario", idUsuario);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_lista) {
                Intent intent = new Intent(PerfilActivity.this, ListaGastosActivity.class);
                intent.putExtra("id_usuario", idUsuario);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_grafico) {
                Intent intent = new Intent(PerfilActivity.this, GraficoActivity.class);
                intent.putExtra("id_usuario", idUsuario);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_perfil) {
                return true;
            }
            return false;
        });
    }
}