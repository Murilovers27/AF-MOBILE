package com.facens.af;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // FIREBASE
    private FirebaseFirestore db;
    private CollectionReference viagensRef;

    // UI
    private EditText etTitulo, etDescricao, etData, etCategoria;
    private CheckBox cbFavorito;
    private TextView tvLatitude, tvLongitude, tvTemperatura, tvClima;
    private Button btnSalvar, btnLocalizacao, btnMapa;

    // GPS
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_CODE = 100;

    private double latitude = 0.0;
    private double longitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 🔥 FIREBASE
        db = FirebaseFirestore.getInstance();
        viagensRef = db.collection("viagens"); // ✅ EXPLÍCITO

        // 🔹 UI
        etTitulo = findViewById(R.id.etTitulo);
        etDescricao = findViewById(R.id.etDescricao);
        etData = findViewById(R.id.etData);
        etCategoria = findViewById(R.id.etCategoria);

        cbFavorito = findViewById(R.id.cbFavorito);

        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tvTemperatura = findViewById(R.id.tvTemperatura);
        tvClima = findViewById(R.id.tvClima);

        btnSalvar = findViewById(R.id.btnSalvar);
        btnLocalizacao = findViewById(R.id.btnLocalizacao);
        btnMapa = findViewById(R.id.btnMapa);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnLocalizacao.setOnClickListener(v -> obterLocalizacao());
        btnSalvar.setOnClickListener(v -> salvar());
        btnMapa.setOnClickListener(v -> abrirMapa());
    }


    private void obterLocalizacao() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {

                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                        tvLatitude.setText("Latitude: " + latitude);
                        tvLongitude.setText("Longitude: " + longitude);
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                obterLocalizacao();
            }
        }
    }


    private void salvar() {

        Map<String, Object> mapa = new HashMap<>();

        mapa.put("titulo", etTitulo.getText().toString());
        mapa.put("descricao", etDescricao.getText().toString());
        mapa.put("data", etData.getText().toString());
        mapa.put("categoria", etCategoria.getText().toString());
        mapa.put("latitude", latitude);
        mapa.put("longitude", longitude);
        mapa.put("temperatura", tvTemperatura.getText().toString());
        mapa.put("clima", tvClima.getText().toString());
        mapa.put("favorito", cbFavorito.isChecked());

        viagensRef.add(mapa)
                .addOnSuccessListener(doc ->
                        Toast.makeText(this, "Salvo!", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro!", Toast.LENGTH_SHORT).show()
                );
    }


    private void abrirMapa() {

        if (latitude != 0.0 && longitude != 0.0) {

            String uri = "geo:" + latitude + "," + longitude;

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(intent);

        } else {
            Toast.makeText(this, "Pegue a localização primeiro", Toast.LENGTH_SHORT).show();
        }
    }
}