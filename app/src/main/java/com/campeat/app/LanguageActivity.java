package com.campeat.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.Locale;

public class LanguageActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private FusedLocationProviderClient fusedLocationClient;

    private ImageView imageBendera;
    private TextView textSelamat, textDatang;
    private Button buttonContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bahasa);

        // INIT VIEW
        imageBendera = findViewById(R.id.image_bendera);
        textSelamat = findViewById(R.id.text_selamat);
        textDatang = findViewById(R.id.text_datang);
        buttonContinue = findViewById(R.id.button_continue);

        // LOCATION CLIENT
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // DEFAULT → INDONESIA (ANTI MIX BAHASA)
        setIndonesia();

        // CEK PERMISSION
        checkLocationPermission();

        // BUTTON
        buttonContinue.setOnClickListener(v -> {
            startActivity(new Intent(LanguageActivity.this, WelcomeActivity.class));
            finish();
        });
    }

    // =====================================================
    // PERMISSION
    // =====================================================

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST
            );
        } else {
            detectLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            detectLocation();
        }
    }

    // =====================================================
    // LOCATION
    // =====================================================

    private void detectLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        detectCountryFromLocation(location);
                    }
                });
    }

    private void detectCountryFromLocation(Location location) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1
            );

            if (addresses != null && !addresses.isEmpty()) {
                String countryCode = addresses.get(0).getCountryCode();

                if ("ID".equals(countryCode)) {
                    setIndonesia();
                } else if ("JP".equals(countryCode)) {
                    setJapan();
                } else {
                    setEnglish();
                }
            }
        } catch (Exception e) {
            setIndonesia(); // fallback
        }
    }

    // =====================================================
    // LANGUAGE SETTER
    // =====================================================

    private void setIndonesia() {
        setLocale("id");
        textSelamat.setText("Selamat ");
        textDatang.setText("Datang");
        buttonContinue.setText("Lanjutkan");
        imageBendera.setImageResource(R.drawable.bendera);
    }

    private void setEnglish() {
        setLocale("en");
        textSelamat.setText("Welcome");
        textDatang.setText("");
        buttonContinue.setText("Continue");
        imageBendera.setImageResource(R.drawable.bendera_uk);
    }

    private void setJapan() {
        setLocale("ja");
        textSelamat.setText("ようこそ");
        textDatang.setText("");
        buttonContinue.setText("続ける");
        imageBendera.setImageResource(R.drawable.bendera_jpn);
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        getResources().updateConfiguration(
                config,
                getResources().getDisplayMetrics()
        );
    }
}
