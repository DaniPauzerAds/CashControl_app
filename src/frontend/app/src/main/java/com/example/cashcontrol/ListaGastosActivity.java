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
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;

public class ListaGastosActivity extends AppCompatActivity {

    ListView lvGastos;
    RequestQueue requestQueue;
    int idUsuario;
    String filtro;
    ArrayList<JSONObject> listaGastos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_gastos);

        idUsuario = getIntent().getIntExtra("id_usuario", 0);
        filtro = getIntent().getStringExtra("filtro");

        if (idUsuario == 0) {
            android.content.SharedPreferences prefs = getSharedPreferences("cashcontrol", MODE_PRIVATE);
            idUsuario = prefs.getInt("id_usuario", 0);
        }

        lvGastos = findViewById(R.id.lvGastos);
        requestQueue = Volley.newRequestQueue(this);

        TextView tvTitulo = findViewById(R.id.tvTituloLista);
        if ("ganho".equals(filtro)) {
            tvTitulo.setText("Meus Ganhos");
        } else {
            tvTitulo.setText("Meus Gastos");
        }

        configurarBottomNav(R.id.nav_lista);
        carregarGastos();
    }

    private void configurarBottomNav(int itemSelecionado) {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(itemSelecionado);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(ListaGastosActivity.this, HomeActivity.class);
                intent.putExtra("id_usuario", idUsuario);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_lista) {
                return true;
            } else if (id == R.id.nav_grafico) {
                Intent intent = new Intent(ListaGastosActivity.this, GraficoActivity.class);
                intent.putExtra("id_usuario", idUsuario);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_perfil) {
                Intent intent = new Intent(ListaGastosActivity.this, PerfilActivity.class);
                intent.putExtra("id_usuario", idUsuario);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void carregarGastos() {
        String url = "https://cashcontrol-app.onrender.com/gastos/" + idUsuario;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    listaGastos.clear();

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject gasto = response.getJSONObject(i);
                            String tipo = gasto.optString("tipo", "gasto");

                            if (filtro == null || filtro.equals(tipo)) {
                                listaGastos.add(gasto);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    lvGastos.setAdapter(new android.widget.BaseAdapter() {
                        @Override
                        public int getCount() { return listaGastos.size(); }
                        @Override
                        public Object getItem(int position) { return listaGastos.get(position); }
                        @Override
                        public long getItemId(int position) { return position; }

                        @Override
                        public View getView(int position, View convertView, android.view.ViewGroup parent) {
                            if (convertView == null) {
                                convertView = LayoutInflater.from(ListaGastosActivity.this)
                                        .inflate(R.layout.item_gasto, parent, false);
                            }
                            try {
                                JSONObject gasto = listaGastos.get(position);
                                TextView tvDescricao = convertView.findViewById(R.id.tvItemDescricao);
                                TextView tvCategoria = convertView.findViewById(R.id.tvItemCategoria);
                                TextView tvData = convertView.findViewById(R.id.tvItemData);
                                TextView tvValor = convertView.findViewById(R.id.tvItemValor);

                                tvDescricao.setText(gasto.getString("descricao"));
                                tvData.setText(gasto.getString("data").substring(0, 10));

                                String tipo = gasto.optString("tipo", "gasto");
                                if ("ganho".equals(tipo)) {
                                    tvValor.setText("+R$ " + String.format("%.2f", gasto.getDouble("valor")));
                                    tvValor.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
                                } else {
                                    tvValor.setText("-R$ " + String.format("%.2f", gasto.getDouble("valor")));
                                    tvValor.setTextColor(android.graphics.Color.parseColor("#F44336"));
                                }

                                String categoria = gasto.getString("categoria");
                                tvCategoria.setText(categoria);

                                switch (categoria) {
                                    case "Alimentação": tvCategoria.setTextColor(android.graphics.Color.parseColor("#4CAF50")); break;
                                    case "Transporte": tvCategoria.setTextColor(android.graphics.Color.parseColor("#2196F3")); break;
                                    case "Moradia": tvCategoria.setTextColor(android.graphics.Color.parseColor("#FF9800")); break;
                                    case "Saúde": tvCategoria.setTextColor(android.graphics.Color.parseColor("#E91E63")); break;
                                    case "Lazer": tvCategoria.setTextColor(android.graphics.Color.parseColor("#9C27B0")); break;
                                    case "Educação": tvCategoria.setTextColor(android.graphics.Color.parseColor("#009688")); break;
                                    case "Roupas": tvCategoria.setTextColor(android.graphics.Color.parseColor("#795548")); break;
                                    default: tvCategoria.setTextColor(android.graphics.Color.parseColor("#F44336")); break;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            return convertView;
                        }
                    });

                    lvGastos.setOnItemLongClickListener((parent, view, position, id) -> {
                        JSONObject gasto = listaGastos.get(position);
                        mostrarOpcoes(gasto);
                        return true;
                    });
                },
                error -> Toast.makeText(ListaGastosActivity.this, "Erro ao buscar gastos!", Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(request);
    }

    private void mostrarOpcoes(JSONObject gasto) {
        try {
            int idGasto = gasto.getInt("id");
            String descricao = gasto.getString("descricao");

            new AlertDialog.Builder(this)
                    .setTitle(descricao)
                    .setItems(new String[]{"Editar", "Deletar"}, (dialog, which) -> {
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
                .setTitle("Deletar")
                .setMessage("Tem certeza que quer deletar?")
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
                    Toast.makeText(ListaGastosActivity.this, "Deletado!", Toast.LENGTH_SHORT).show();
                    carregarGastos();
                },
                error -> Toast.makeText(ListaGastosActivity.this, "Erro ao deletar!", Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(request);
    }

    private void mostrarDialogEditar(JSONObject gasto) {
        try {
            int idGasto = gasto.getInt("id");

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

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
                new DatePickerDialog(ListaGastosActivity.this,
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
                    Toast.makeText(ListaGastosActivity.this, "Editado!", Toast.LENGTH_SHORT).show();
                    carregarGastos();
                },
                error -> Toast.makeText(ListaGastosActivity.this, "Erro ao editar!", Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(request);
    }
}