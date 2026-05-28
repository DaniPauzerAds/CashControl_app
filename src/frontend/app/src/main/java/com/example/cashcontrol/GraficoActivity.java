package com.example.cashcontrol;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.json.JSONObject;
import java.util.ArrayList;

public class GraficoActivity extends AppCompatActivity {

    BarChart barChart;
    TextView txtTotal;
    RequestQueue queue;
    int idUsuario;

    String URL_BASE = "https://cashcontrol-app.onrender.com/gastos/resumo/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grafico);

        idUsuario = getIntent().getIntExtra("id_usuario", 0);
        if (idUsuario == 0) {
            android.content.SharedPreferences prefs = getSharedPreferences("cashcontrol", MODE_PRIVATE);
            idUsuario = prefs.getInt("id_usuario", 0);
        }

        barChart = findViewById(R.id.barChart);
        txtTotal = findViewById(R.id.txtTotal);
        queue = Volley.newRequestQueue(this);

        configurarBottomNav(R.id.nav_grafico);
        carregarGrafico();
    }

    private void configurarBottomNav(int itemSelecionado) {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(itemSelecionado);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(GraficoActivity.this, HomeActivity.class);
                intent.putExtra("id_usuario", idUsuario);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_lista) {
                Intent intent = new Intent(GraficoActivity.this, ListaGastosActivity.class);
                intent.putExtra("id_usuario", idUsuario);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_grafico) {
                return true;
            } else if (id == R.id.nav_perfil) {
                Intent intent = new Intent(GraficoActivity.this, PerfilActivity.class);
                intent.putExtra("id_usuario", idUsuario);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void carregarGrafico() {
        String url = URL_BASE + idUsuario;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        ArrayList<BarEntry> entradas = new ArrayList<>();
                        ArrayList<String> categorias = new ArrayList<>();
                        ArrayList<Integer> cores = new ArrayList<>();
                        float totalGeral = 0;

                        int[] coresArray = {
                                Color.parseColor("#4CAF50"),
                                Color.parseColor("#2196F3"),
                                Color.parseColor("#FF9800"),
                                Color.parseColor("#E91E63"),
                                Color.parseColor("#9C27B0"),
                                Color.parseColor("#009688"),
                                Color.parseColor("#795548"),
                                Color.parseColor("#F44336")
                        };

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            String categoria = obj.getString("categoria");
                            float total = (float) obj.getDouble("total_gastos");
                            entradas.add(new BarEntry(i, total));
                            categorias.add(categoria);
                            cores.add(coresArray[i % coresArray.length]);
                            totalGeral += total;
                        }

                        txtTotal.setText("R$ " + String.format("%.2f", totalGeral));

                        BarDataSet dataSet = new BarDataSet(entradas, "");
                        dataSet.setColors(cores);
                        dataSet.setValueTextColor(Color.BLACK);
                        dataSet.setValueTextSize(11f);

                        BarData barData = new BarData(dataSet);
                        barData.setBarWidth(0.6f);

                        barChart.setData(barData);
                        barChart.getDescription().setEnabled(false);
                        barChart.setFitBars(true);
                        barChart.getLegend().setEnabled(false);
                        barChart.getAxisRight().setEnabled(false);
                        barChart.getAxisLeft().setTextColor(Color.BLACK);
                        barChart.getAxisLeft().setTextSize(10f);

                        XAxis xAxis = barChart.getXAxis();
                        xAxis.setValueFormatter(new IndexAxisValueFormatter(categorias));
                        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                        xAxis.setGranularity(1f);
                        xAxis.setTextColor(Color.BLACK);
                        xAxis.setTextSize(10f);
                        xAxis.setDrawGridLines(false);

                        barChart.animateY(1400);
                        barChart.invalidate();

                    } catch (Exception e) {
                        Toast.makeText(GraficoActivity.this, "Erro ao processar gráfico", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(GraficoActivity.this, "Erro ao buscar dados", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }
}///////////////////////=