package com.example.cashcontrol;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;

public class ListaGastosActivity extends AppCompatActivity {

    ListView lvGastos;
    RequestQueue requestQueue;
    int idUsuario;
    ArrayList<JSONObject> listaGastos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_gastos);

        lvGastos = findViewById(R.id.lvGastos);
        requestQueue = Volley.newRequestQueue(this);
        idUsuario = getIntent().getIntExtra("id_usuario", 0);

        if (idUsuario == 0) {
            android.content.SharedPreferences prefs = getSharedPreferences("cashcontrol", MODE_PRIVATE);
            idUsuario = prefs.getInt("id_usuario", 0);
        }

        carregarGastos();
    }

    private void carregarGastos() {
        String url = "https://cashcontrol-app.onrender.com/gastos/" + idUsuario;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    listaGastos.clear();
                    ArrayList<String> itens = new ArrayList<>();

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject gasto = response.getJSONObject(i);
                            listaGastos.add(gasto);
                            String descricao = gasto.getString("descricao");
                            String categoria = gasto.getString("categoria");
                            double valor = gasto.getDouble("valor");
                            String data = gasto.getString("data").substring(0, 10);
                            itens.add(descricao + "\n" + categoria + " • " + data + "\nR$ " + String.format("%.2f", valor));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_list_item_1,
                            itens
                    );
                    lvGastos.setAdapter(adapter);

                    // Long press pra editar ou deletar
                    lvGastos.setOnItemLongClickListener((parent, view, position, id) -> {
                        JSONObject gasto = listaGastos.get(position);
                        mostrarOpcoes(gasto);
                        return true;
                    });
                },
                error -> Toast.makeText(this, "Erro ao buscar gastos!", Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(request);
    }

    private void mostrarOpcoes(JSONObject gasto) {
        try {
            int idGasto = gasto.getInt("id");
            String descricao = gasto.getString("descricao");

            new AlertDialog.Builder(this)
                    .setTitle(descricao)
                    .setItems(new String[]{" Editar", " Deletar"}, (dialog, which) -> {
                        if (which == 0) {
                            mostrarDialogEditar(gasto);
                        } else {
                            confirmarDelete(idGasto);
                        }
                    })
                    .show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void confirmarDelete(int idGasto) {
        new AlertDialog.Builder(this)
                .setTitle("Deletar gasto")
                .setMessage("Tem certeza que quer deletar esse gasto?")
                .setPositiveButton("Deletar", (dialog, which) -> deletarGasto(idGasto))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deletarGasto(int idGasto) {
        String url = "https://cashcontrol-app.onrender.com/gastos/" + idGasto;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                response -> {
                    Toast.makeText(this, "Gasto deletado!", Toast.LENGTH_SHORT).show();
                    carregarGastos();
                },
                error -> Toast.makeText(this, "Erro ao deletar!", Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(request);
    }

    private void mostrarDialogEditar(JSONObject gasto) {
        try {
            int idGasto = gasto.getInt("id");

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Editar Gasto");

            View view = LayoutInflater.from(this).inflate(R.layout.activity_editar_gasto, null);
            builder.setView(view);

            EditText etDescricao = view.findViewById(R.id.etDescricao);
            EditText etValor = view.findViewById(R.id.etValor);
            EditText etData = view.findViewById(R.id.etData);
            Spinner spinnerCategoria = view.findViewById(R.id.spinnerEditCategoria);

            etDescricao.setText(gasto.getString("descricao"));
            etValor.setText(String.valueOf(gasto.getDouble("valor")));
            etData.setText(gasto.getString("data").substring(0, 10));

            String[] categorias = {"Alimentação", "Transporte", "Moradia", "Saúde", "Lazer", "Educação", "Roupas", "Outros"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categorias);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategoria.setAdapter(adapter);

            String categoriaAtual = gasto.getString("categoria");
            for (int i = 0; i < categorias.length; i++) {
                if (categorias[i].equals(categoriaAtual)) {
                    spinnerCategoria.setSelection(i);
                    break;
                }
            }

            etData.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                new DatePickerDialog(this,
                        (datePicker, year, month, day) -> {
                            String data = String.format("%04d-%02d-%02d", year, month + 1, day);
                            etData.setText(data);
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                ).show();
            });

            builder.setPositiveButton("Salvar", (dialog, which) -> {
                String descricao = etDescricao.getText().toString().trim();
                String valor = etValor.getText().toString().trim();
                String data = etData.getText().toString().trim();
                String categoria = spinnerCategoria.getSelectedItem().toString();
                editarGasto(idGasto, descricao, valor, categoria, data);
            });

            builder.setNegativeButton("Cancelar", null);
            builder.show();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void editarGasto(int idGasto, String descricao, String valor, String categoria, String data) {
        String url = "https://cashcontrol-app.onrender.com/gastos/" + idGasto;

        JSONObject body = new JSONObject();
        try {
            body.put("descricao", descricao);
            body.put("valor", Double.parseDouble(valor));
            body.put("categoria", categoria);
            body.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                body,
                response -> {
                    Toast.makeText(this, "Gasto editado!", Toast.LENGTH_SHORT).show();
                    carregarGastos();
                },
                error -> Toast.makeText(this, "Erro ao editar!", Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(request);
    }
}