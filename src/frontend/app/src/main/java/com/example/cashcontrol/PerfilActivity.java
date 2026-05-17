package com.example.cashcontrol;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PerfilActivity extends AppCompatActivity {

    TextView tvNomePerfil, tvEmailPerfil;
    Button btnSairConta;
    int idUsuario;

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

        SharedPreferences prefs = getSharedPreferences("cashcontrol", MODE_PRIVATE);
        String nome = prefs.getString("nome", "");
        String email = prefs.getString("email", "");

        tvNomePerfil.setText(nome);
        tvEmailPerfil.setText(email);

        configurarBottomNav(R.id.nav_perfil);

        btnSairConta.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(PerfilActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
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