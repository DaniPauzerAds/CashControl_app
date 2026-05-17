package com.example.cashcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    TextView tvBemVindo, tvTotal;
    ListView lvCategorias;
    FloatingActionButton btnAdicionarGasto;
    RequestQueue requestQueue;
    int idUsuario;
    String nomeUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvBemVindo = findViewById(R.id.tvBemVindo);
        tvTotal = findViewById(R.id.tvTotal);
        lvCategorias = findViewById(R.id.lvCategorias);
        btnAdicionarGasto = findViewById(R.id.btnAdicionarGasto);
        requestQueue = Volley.newRequestQueue(this);

        nomeUsuario = getIntent().getStringExtra("nome");
        idUsuario = getIntent().getIntExtra("id_usuario", 0);

        tvBemVindo.setText("Olá, " + nomeUsuario + "!");

        carregarResumo();

        btnAdicionarGasto.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AdicionarGastoActivity.class);
            intent.putExtra("id_usuario", idUsuario);
            startActivity(intent);
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_lista) {
                Intent intent = new Intent(HomeActivity.this, ListaGastosActivity.class);
                intent.putExtra("id_usuario", idUsuario);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_grafico) {
                Intent intent = new Intent(HomeActivity.this, GraficoActivity.class);
                intent.putExtra("id_usuario", idUsuario);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_perfil) {
                Intent intent = new Intent(HomeActivity.this, PerfilActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarResumo();
    }

    private void carregarResumo() {
        String url = "https://cashcontrol-app.onrender.com/gastos/resumo/" + idUsuario;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    ArrayList<String> categorias = new ArrayList<>();
                    double total = 0;

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject item = response.getJSONObject(i);
                            String categoria = item.getString("categoria");
                            double valor = item.getDouble("total");
                            total += valor;
                            categorias.add(categoria + ":  R$ " + String.format("%.2f", valor));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    tvTotal.setText("R$ " + String.format("%.2f", total));

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this,
                            R.layout.lista_categorias,
                            categorias
                    );

                    lvCategorias.setAdapter(adapter);
                },
                error -> {
                    tvTotal.setText("R$ 0,00");
                }
        );

        requestQueue.add(request);
    }
}