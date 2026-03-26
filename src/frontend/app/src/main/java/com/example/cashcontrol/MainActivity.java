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

public class MainActivity extends AppCompatActivity {

    EditText etEmail, etSenha;
    Button btnLogin;
    TextView tvCadastro, tvEsqueceu;
    RequestQueue requestQueue;

    String URL = "https://cashcontrol-app.onrender.com/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.etEmail);
        etSenha = findViewById(R.id.etSenha);
        btnLogin = findViewById(R.id.btnEntrar);
        tvCadastro = findViewById(R.id.tvCadastro);
        tvEsqueceu = findViewById(R.id.tvEsqueceu);
        requestQueue = Volley.newRequestQueue(this);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String senha = etSenha.getText().toString().trim();

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            fazerLogin(email, senha);
        });

        tvCadastro.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CadastroActivity.class);
            startActivity(intent);
        });
    }

    private void fazerLogin(String email, String senha) {
        JSONObject body = new JSONObject();
        try {
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
                    try {
                        String nome = response.getString("usuario");
                        Toast.makeText(this, "Bem-vinda, " + nome + "!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        intent.putExtra("nome", nome);
                        startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(this, "Email ou senha inválidos!", Toast.LENGTH_SHORT).show();
                }
        );

        requestQueue.add(request);
    }
}