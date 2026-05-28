package com.example.cashcontrol;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
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
import android.view.View;

public class AdicionarGastoActivity extends AppCompatActivity {

    EditText etDescricao, etValor, etData;
    Spinner spinnerCategoria;
    Button btnSalvar, btnTipoGasto, btnTipoGanho;
    RequestQueue requestQueue;
    int idUsuario;
    String tipoSelecionado = "gasto";

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
        btnTipoGasto = findViewById(R.id.btnTipoGasto);
        btnTipoGanho = findViewById(R.id.btnTipoGanho);
        requestQueue = Volley.newRequestQueue(this);

        idUsuario = getIntent().getIntExtra("id_usuario", 0);
        if (idUsuario == 0) {
            android.content.SharedPreferences prefs = getSharedPreferences("cashcontrol", MODE_PRIVATE);
            idUsuario = prefs.getInt("id_usuario", 0);
        }

        String[] categorias = {
                "Alimentação", "Transporte", "Moradia",
                "Saúde", "Lazer", "Educação", "Roupas",
                "Outros", "➕ Adicionar categoria..."
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categorias);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapter);

        spinnerCategoria.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position == categorias.length - 1) {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(AdicionarGastoActivity.this);
                    builder.setTitle("Nova categoria");
                    EditText etNovaCategoria = new EditText(AdicionarGastoActivity.this);
                    etNovaCategoria.setHint("Ex: Academia, Pets...");
                    builder.setView(etNovaCategoria);
                    builder.setPositiveButton("Adicionar", (dialog, which) -> {
                        String novaCategoria = etNovaCategoria.getText().toString().trim();
                        if (!novaCategoria.isEmpty()) {
                            String[] novasCategorias = new String[categorias.length];
                            System.arraycopy(categorias, 0, novasCategorias, 0, categorias.length - 1);
                            novasCategorias[categorias.length - 1] = novaCategoria;
                            novasCategorias = java.util.Arrays.copyOf(novasCategorias, novasCategorias.length + 1);
                            novasCategorias[novasCategorias.length - 1] = "➕ Adicionar categoria...";
                            ArrayAdapter<String> novoAdapter = new ArrayAdapter<>(AdicionarGastoActivity.this, android.R.layout.simple_spinner_item, novasCategorias);
                            novoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerCategoria.setAdapter(novoAdapter);
                            spinnerCategoria.setSelection(novasCategorias.length - 2);
                        }
                    });
                    builder.setNegativeButton("Cancelar", null);
                    builder.show();
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        btnTipoGasto.setOnClickListener(v -> {
            tipoSelecionado = "gasto";
            btnTipoGasto.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#8E0235")));
            btnTipoGasto.setTextColor(Color.WHITE);
            btnTipoGanho.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F7F9FC")));
            btnTipoGanho.setTextColor(Color.parseColor("#8E0235"));
            btnSalvar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#8E0235")));
        });

        btnTipoGanho.setOnClickListener(v -> {
            tipoSelecionado = "ganho";
            btnTipoGanho.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            btnTipoGanho.setTextColor(Color.WHITE);
            btnTipoGasto.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F7F9FC")));
            btnTipoGasto.setTextColor(Color.parseColor("#8E0235"));
            btnSalvar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        });

        etData.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        String data = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                        etData.setText(data);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
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
            salvarGasto(descricao, valor, categoria, dataFormatada, tipoSelecionado);
        });
    }

    private void salvarGasto(String descricao, String valor, String categoria, String data, String tipo) {
        JSONObject body = new JSONObject();
        try {
            body.put("descricao", descricao);
            body.put("valor", Double.parseDouble(valor.replace(",", ".")));
            body.put("categoria", categoria);
            body.put("data", data);
            body.put("id_usuario", idUsuario);
            body.put("tipo", tipo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                body,
                response -> {
                    Toast.makeText(this, tipo.equals("ganho") ? "Ganho salvo!" : "Gasto salvo!", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> Toast.makeText(this, "Erro ao salvar!", Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(request);
    }
}