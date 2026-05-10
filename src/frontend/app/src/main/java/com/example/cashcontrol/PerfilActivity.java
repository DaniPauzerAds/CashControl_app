package com.example.cashcontrol;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PerfilActivity extends AppCompatActivity {

    TextView tvNomePerfil, tvEmailPerfil;
    Button btnSairConta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        tvNomePerfil = findViewById(R.id.tvNomePerfil);
        tvEmailPerfil = findViewById(R.id.tvEmailPerfil);
        btnSairConta = findViewById(R.id.btnSairConta);

        SharedPreferences prefs = getSharedPreferences("cashcontrol", MODE_PRIVATE);
        String nome = prefs.getString("nome", "");
        String email = prefs.getString("email", "");

        tvNomePerfil.setText(nome);
        tvEmailPerfil.setText(email);

        btnSairConta.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(PerfilActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}