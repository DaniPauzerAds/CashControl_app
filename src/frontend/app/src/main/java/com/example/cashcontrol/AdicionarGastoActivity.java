package com.example.cashcontrol;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Calendar;

public class AdicionarGastoActivity extends AppCompatActivity {

    EditText etDescricao, etValor, etData;
    Spinner spinnerCategoria;
    Button btnSalvar, btnVoltar;
    RequestQueue requestQueue;
    int idUsuario;

    String URL = "https://cashcontrol-app.onrender.com/gastos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionargasto);

        etDescricao = findViewById(R.id.etDescricao);
        etValor = findViewById(R.id.etValor);
        etData = findViewById(R.id.etData);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnVoltar = findViewById(R.id.btnVoltar);
        requestQueue = Volley.newRequestQueue(this);

        idUsuario = getIntent().getIntExtra("id_usuario", 0);

        String[] categorias = {
                "Alimentação",
                "Transporte",
                "Moradia",
                "Saúde",
                "Lazer",
                "Educação",
                "Roupas",
                "Outros"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categorias
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapter);

        etData.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int dia = calendar.get(Calendar.DAY_OF_MONTH);
            int mes = calendar.get(Calendar.MONTH);
            int ano = calendar.get(Calendar.YEAR);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    AdicionarGastoActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        String dataSelecionada = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                        etData.setText(dataSelecionada);
                    },
                    ano, mes, dia
            );
            datePickerDialog.show();
        });

        btnVoltar.setOnClickListener(v -> {
            finish();
        });

        btnSalvar.setOnClickListener(v -> {
            String descricao = etDescricao.getText().toString().trim();
            String valor = etValor.getText().toString().trim();
            String categoria = spinnerCategoria.getSelectedItem().toString();
            String data = etData.getText().toString().trim();

            if (descricao.isEmpty() || valor.isEmpty() || data.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            String dataFormatada = data.substring(6) + "-" + data.substring(3, 5) + "-" + data.substring(0, 2);

            salvarGasto(descricao, valor, categoria, dataFormatada);
        });
    }

    private void salvarGasto(String descricao, String valor, String categoria, String data) {
        JSONObject body = new JSONObject();
        try {
            body.put("descricao", descricao);
            body.put("valor", Double.parseDouble(valor));
            body.put("categoria", categoria);
            body.put("data", data);
            body.put("id_usuario", idUsuario);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                body,
                response -> {
                    Toast.makeText(this, "Gasto salvo com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    Toast.makeText(this, "Erro ao salvar gasto!", Toast.LENGTH_SHORT).show();
                }
        );

        requestQueue.add(request);
    }
}