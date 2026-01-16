package com.campeat.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.campeat.app.utils.PointManager;

import com.campeat.app.utils.CartManager;

import java.text.NumberFormat;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {

    private TextView textTotal;
    private Button btnConfirm;
    private RadioGroup radioGroup;

    private String selectedPayment = null;
    private double total = 0; // <-- JADI FIELD

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        textTotal = findViewById(R.id.text_checkout_total);
        btnConfirm = findViewById(R.id.btn_confirm_checkout);
        radioGroup = findViewById(R.id.radio_payment_group);

        // Ambil total sekali saja
        total = getIntent().getDoubleExtra("total", 0);
        textTotal.setText(formatRupiah(total));

        btnConfirm.setEnabled(false);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            btnConfirm.setEnabled(true);
            if (checkedId == R.id.radio_ewallet) selectedPayment = "E-Wallet";
            else if (checkedId == R.id.radio_transfer) selectedPayment = "Transfer Bank";
            else if (checkedId == R.id.radio_cash) selectedPayment = "Cash";
        });

        btnConfirm.setOnClickListener(v -> {
            if (selectedPayment == null) {
                Toast.makeText(this, getString(R.string.payment_choose_warning), Toast.LENGTH_SHORT).show();
                return;
            }

            int rewardPoint = (int) (total / 10000); // 1 point per 10rb

            PointManager.addPoint(this, rewardPoint);
            CartManager.clearCart();

            Toast.makeText(this,
                    "Pembayaran via " + selectedPayment + " berhasil! +" + rewardPoint + " point 🎉",
                    Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private String formatRupiah(double value) {
        Locale localeID = new Locale("in", "ID");
        NumberFormat format = NumberFormat.getCurrencyInstance(localeID);
        return format.format(value).replace("Rp", "Rp ").trim();
    }
}

