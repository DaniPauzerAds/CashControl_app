package com.example.cashcontrol;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TextView tvBemVinda = findViewById(R.id.tvBemVinda);
        String nome = getIntent().getStringExtra("nome");
        if (nome != null) {
            tvBemVinda.setText("Bem-vinda, " + nome + "! 💙");
        }
    }
}