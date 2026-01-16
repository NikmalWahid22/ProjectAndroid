package com.campeat.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.campeat.app.utils.Prefs;
import com.campeat.app.utils.PointManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {

    TextView tvName, tvUni, tvPoints, textLogout;

    // TAMBAHAN
    TextView tvStatusTitle, tvStatusValue;

    View btnEditProfile, btnLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // VIEW
        tvName = findViewById(R.id.text_user_name);
        tvUni = findViewById(R.id.text_user_campus);
        tvPoints = findViewById(R.id.text_stat_value);
        btnEditProfile = findViewById(R.id.btn_my_account);

        textLogout = findViewById(R.id.text_logout);

        // TAMBAHAN: ambil view card kanan
        View cardStatus = findViewById(R.id.card_status);
        if (cardStatus != null) {
            tvStatusTitle = cardStatus.findViewById(R.id.text_stat_title);
            tvStatusValue = cardStatus.findViewById(R.id.text_stat_value);

            if (tvStatusTitle != null) tvStatusTitle.setText("Status");
            if (tvStatusValue != null) tvStatusValue.setText("Active");
        }

        loadProfile();

        // EDIT PROFILE
        btnEditProfile.setOnClickListener(v -> showEditDialog());

        // LOGOUT
        textLogout.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // BOTTOM NAVIGATION
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_bar);
        bottomNav.setSelectedItemId(R.id.nav_profile);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            }

            if (id == R.id.nav_search) {
                startActivity(new Intent(this, SearchActivity.class));
                finish();
                return true;
            }

            if (id == R.id.nav_cart) {
                startActivity(new Intent(this, CartActivity.class));
                finish();
                return true;
            }

            if (id == R.id.nav_profile) {
                return true;
            }

            return false;
        });
    }

    private void loadProfile() {
        tvName.setText(Prefs.getName(this));
        tvUni.setText(Prefs.getUniversity(this));
        tvPoints.setText(PointManager.getPoint(this) + " pts");
    }

    private void showEditDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        EditText etName = view.findViewById(R.id.et_name);
        EditText etUni = view.findViewById(R.id.et_uni);

        etName.setText(Prefs.getName(this));
        etUni.setText(Prefs.getUniversity(this));

        new AlertDialog.Builder(this)
                .setTitle("Edit Profil")
                .setView(view)
                .setPositiveButton("Simpan", (d,w)->{
                    Prefs.saveName(this, etName.getText().toString());
                    Prefs.saveUniversity(this, etUni.getText().toString());
                    loadProfile();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void restartApp() {
        Intent i = new Intent(this, LanguageActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}
