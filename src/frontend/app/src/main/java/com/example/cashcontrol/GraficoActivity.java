package com.example.cashcontrol;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import org.json.JSONObject;

import java.util.ArrayList;

public class GraficoActivity extends AppCompatActivity {

    PieChart pieChart;
    TextView txtTotal;
    Button btnVoltar;
    RequestQueue queue;
    int idUsuario;

    String URL_BASE = "https://cashcontrol-app.onrender.com/gastos/resumo/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grafico);

        pieChart = findViewById(R.id.pieChart);
        txtTotal = findViewById(R.id.txtTotal);
        btnVoltar = findViewById(R.id.btnVoltar);
        queue = Volley.newRequestQueue(this);

        btnVoltar.setOnClickListener(v -> {
            finish();
        });
        idUsuario = getIntent().getIntExtra("id_usuario", 0);
        if (idUsuario == 0) {
            android.content.SharedPreferences prefs = getSharedPreferences("cashcontrol", MODE_PRIVATE);
            idUsuario = prefs.getInt("id_usuario", 0);
        }
        carregarGrafico();
    }

    private void carregarGrafico() {

        String url = URL_BASE + idUsuario;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,

                response -> {
                    try {

                        ArrayList<PieEntry> lista = new ArrayList<>();
                        ArrayList<Integer> cores = new ArrayList<>();

                        float totalGeral = 0;

                        for (int i = 0; i < response.length(); i++) {

                            JSONObject obj = response.getJSONObject(i);

                            String categoria = obj.getString("categoria");
                            float total = (float) obj.getDouble("total");

                            lista.add(new PieEntry(total, categoria));
                            totalGeral += total;
                        }

                        cores.add(Color.parseColor("#4CAF50"));
                        cores.add(Color.parseColor("#2196F3"));
                        cores.add(Color.parseColor("#FF9800"));
                        cores.add(Color.parseColor("#E91E63"));
                        cores.add(Color.parseColor("#9C27B0"));
                        cores.add(Color.parseColor("#009688"));
                        cores.add(Color.parseColor("#795548"));
                        cores.add(Color.parseColor("#F44336"));

                        PieDataSet dataSet = new PieDataSet(lista, "");
                        dataSet.setColors(cores);
                        dataSet.setSliceSpace(3f);
                        dataSet.setValueTextColor(Color.WHITE);
                        dataSet.setValueTextSize(14f);

                        PieData data = new PieData(dataSet);

                        pieChart.setData(data);
                        pieChart.getDescription().setEnabled(false);
                        pieChart.setDrawHoleEnabled(true);
                        pieChart.setHoleRadius(58f);
                        pieChart.setTransparentCircleRadius(63f);

                        pieChart.setCenterText("Tudo\nR$ " + totalGeral);
                        pieChart.setCenterTextSize(20f);

                        txtTotal.setText("R$ " + totalGeral);

                        Legend legend = pieChart.getLegend();
                        legend.setTextSize(14f);
                        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
                        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
                        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
                        legend.setDrawInside(false);

                        pieChart.animateY(1400);
                        pieChart.invalidate();

                    } catch (Exception e) {
                        Toast.makeText(this, "Erro ao processar gráfico", Toast.LENGTH_SHORT).show();
                    }
                },

                error -> Toast.makeText(this, "Erro ao buscar dados", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }
}