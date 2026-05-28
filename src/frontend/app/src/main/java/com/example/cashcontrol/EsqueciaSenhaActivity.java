package com.example.cashcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

public class EsqueciaSenhaActivity extends AppCompatActivity {

    EditText etEmailRedefinir, etNovaSenha, etConfirmarNovaSenha;
    Button btnRedefinir;
    TextView tvVoltarLogin;
    RequestQueue requestQueue;

    String URL = "https://cashcontrol-app.onrender.com/redefinir-senha";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esqueciasenha);

        etEmailRedefinir = findViewById(R.id.etEmailRedefinir);
        etNovaSenha = findViewById(R.id.etNovaSenha);
        etConfirmarNovaSenha = findViewById(R.id.etConfirmarNovaSenha);
        btnRedefinir = findViewById(R.id.btnRedefinir);
        tvVoltarLogin = findViewById(R.id.tvVoltarLogin);
        requestQueue = Volley.newRequestQueue(this);

        btnRedefinir.setOnClickListener(v -> {
            String email = etEmailRedefinir.getText().toString().trim();
            String novaSenha = etNovaSenha.getText().toString().trim();
            String confirmarSenha = etConfirmarNovaSenha.getText().toString().trim();

            if (email.isEmpty() || novaSenha.isEmpty() || confirmarSenha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Digite um email válido!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (novaSenha.length() < 6) {
                Toast.makeText(this, "A senha deve ter no mínimo 6 caracteres!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!novaSenha.equals(confirmarSenha)) {
                Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show();
                return;
            }

            redefinirSenha(email, novaSenha);
        });

        tvVoltarLogin.setOnClickListener(v -> {
            Intent intent = new Intent(EsqueciaSenhaActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void redefinirSenha(String email, String novaSenha) {
        JSONObject body = new JSONObject();
        try {
            body.put("email", email);
            body.put("novaSenha", novaSenha);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                URL,
                body,
                response -> {
                    Toast.makeText(this, "Senha redefinida com sucesso!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EsqueciaSenhaActivity.this, MainActivity.class);
                    startActivity(intent);
                },
                error -> {
                    Toast.makeText(this, "Email não encontrado!", Toast.LENGTH_SHORT).show();
                }
        );

        requestQueue.add(request);
    }
}