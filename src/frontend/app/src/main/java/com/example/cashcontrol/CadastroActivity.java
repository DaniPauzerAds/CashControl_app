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

public class CadastroActivity extends AppCompatActivity {

    EditText etNome, etEmail, etSenha;
    Button btnCriarConta;
    TextView tvLogin;
    RequestQueue requestQueue;

    String URL = "https://cashcontrol-app.onrender.com/cadastro";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        etNome = findViewById(R.id.etNome);
        etEmail = findViewById(R.id.etEmail);
        etSenha = findViewById(R.id.etSenha);
        btnCriarConta = findViewById(R.id.btnCriarConta);
        tvLogin = findViewById(R.id.tvLogin);
        requestQueue = Volley.newRequestQueue(this);

        btnCriarConta.setOnClickListener(v -> {
            String nome = etNome.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String senha = etSenha.getText().toString().trim();

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Digite um email válido!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (senha.length() < 6) {
                Toast.makeText(this, "A senha deve ter no mínimo 6 caracteres!", Toast.LENGTH_SHORT).show();
                return;
            }

            fazerCadastro(nome, email, senha);
        });
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(CadastroActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }
    private void fazerCadastro(String nome, String email, String senha) {
        JSONObject body = new JSONObject();
        try {
            body.put("nome", nome);
            body.put("email", email);
            body.put("senha", senha);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                body,
                response -> {
                    Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CadastroActivity.this, MainActivity.class);
                    startActivity(intent);
                },
                error -> {
                    Toast.makeText(this, "Erro ao cadastrar! Email já existe.", Toast.LENGTH_SHORT).show();
                }
        );

        requestQueue.add(request);
    }
}