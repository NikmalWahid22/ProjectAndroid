package com.campeat.app;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.campeat.app.adapter.OrderAdminAdapter;
import com.campeat.app.model.Order;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeAdminActivity extends AppCompatActivity {

    // ================================
    // TEXT VIEW
    // ================================
    private TextView tvTotalOrders;
    private TextView tvPendingOrders;
    private TextView tvProcessOrders;
    private TextView tvSuccessOrders;
    private TextView tvDailyRevenue;

    private TextView tvOrderGrowth;
    private TextView tvRevenueGrowth;

    // ================================
    // BUTTON
    // ================================
    private Button btnExportReport;

    // ================================
    // NAVIGATION
    // ================================
    private LinearLayout navHome;
    private LinearLayout navMenu;
    private LinearLayout navProfile;

    // ================================
    // RECYCLER VIEW
    // ================================
    private RecyclerView rvOrders;

    // ================================
    // LIST
    // ================================
    private List<Order> orderList;

    // ================================
    // ADAPTER
    // ================================
    private OrderAdminAdapter adapter;

    // ================================
    // FIREBASE
    // ================================
    private DatabaseReference ordersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_admin);

        getWindow().setStatusBarColor(Color.parseColor("#EEF8F2"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        initViews();
        setupRecyclerView();
        setupBottomNav();
        setupExportButton();
        loadOrdersRealtime();
    }

    // ================================
    // INIT VIEW
    // ================================
    private void initViews() {

        tvTotalOrders = findViewById(R.id.tv_total_orders);
        tvPendingOrders = findViewById(R.id.tv_pending_orders);
        tvProcessOrders = findViewById(R.id.tv_process_orders);
        tvSuccessOrders = findViewById(R.id.tv_success_orders);
        tvDailyRevenue = findViewById(R.id.tv_daily_revenue);
        tvOrderGrowth = findViewById(R.id.tv_order_growth);
        tvRevenueGrowth = findViewById(R.id.tv_revenue_growth);

        btnExportReport = findViewById(R.id.btn_export_report);

        navHome = findViewById(R.id.nav_home);
        navMenu = findViewById(R.id.nav_orders);
        navProfile = findViewById(R.id.nav_profile);

        rvOrders = findViewById(R.id.rv_orders);

        orderList = new ArrayList<>();

        ordersRef = FirebaseDatabase.getInstance(
                ""
        ).getReference("orders");
    }

    // ================================
    // SETUP RECYCLER VIEW
    // ================================
    private void setupRecyclerView() {

        adapter = new OrderAdminAdapter(
                this,
                orderList,
                order -> {

                    Intent intent = new Intent(
                            HomeAdminActivity.this,
                            OrderDetailAdminActivity.class
                    );

                    intent.putExtra("orderId", order.getOrderId());
                    intent.putExtra("uid", order.getUid());

                    intent.putExtra(
                            "date",
                            order.getDate()
                    );

                    intent.putExtra(
                            "status",
                            order.getStatus()
                    );

                    intent.putExtra(
                            "customerName",
                            order.getCustomerName()
                    );

                    intent.putExtra(
                            "customerContact",
                            order.getCustomerContact()
                    );

                    intent.putExtra(
                            "paymentMethod",
                            order.getPayment()
                    );

                    intent.putExtra(
                            "pickupMethod",
                            order.getDeliveryMethod()
                    );

                    startActivity(intent);
                }
        );

        rvOrders.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rvOrders.setAdapter(adapter);

        rvOrders.setNestedScrollingEnabled(false);
    }

    // ================================
    // BOTTOM NAVIGATION
    // ================================
    private void setupBottomNav() {

        navHome.setOnClickListener(v -> {
            // already in home
        });

        navMenu.setOnClickListener(v ->
                startActivity(
                        new Intent(
                                HomeAdminActivity.this,
                                ManageMenuAdminActivity.class
                        )
                )
        );

        navProfile.setOnClickListener(v ->
                startActivity(
                        new Intent(
                                HomeAdminActivity.this,
                                ProfileAdminActivity.class
                        )
                )
        );
    }

    // ================================
    // EXPORT BUTTON
    // ================================
    private void setupExportButton() {

        btnExportReport.setOnClickListener(v -> {
            exportPdfReport();
        });
    }

    // ================================
    // LOAD ORDERS REALTIME
    // ================================
    private void loadOrdersRealtime() {

        ordersRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                orderList.clear();

                int pending = 0;
                int process = 0;
                int success = 0;

                double revenue = 0;

                // =========================
                // TODAY VS YESTERDAY
                // =========================
                int todayOrders = 0;
                int yesterdayOrders = 0;

                double todayRevenue = 0;
                double yesterdayRevenue = 0;

                java.text.SimpleDateFormat sdf =
                        new java.text.SimpleDateFormat(
                                "dd MMM yyyy",
                                java.util.Locale.getDefault()
                        );

                java.util.Calendar calendar =
                        java.util.Calendar.getInstance();

                String today =
                        sdf.format(calendar.getTime());

                calendar.add(java.util.Calendar.DAY_OF_MONTH, -1);

                String yesterday =
                        sdf.format(calendar.getTime());

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {

                    for (DataSnapshot orderSnapshot : userSnapshot.getChildren()) {

                        Order order =
                                orderSnapshot.getValue(Order.class);

                        if (order != null) {

                            order.setOrderId(orderSnapshot.getKey());
                            order.setUid(userSnapshot.getKey());

                            orderList.add(order);

                            String status = order.getStatus();

                            // =========================
                            // STATUS COUNT
                            // =========================
                            if (status != null) {

                                switch (status) {

                                    case "Pending":
                                        pending++;
                                        break;

                                    case "Process":
                                        process++;
                                        break;

                                    case "Done":

                                        success++;

                                        revenue += order.getTotal();

                                        break;
                                }
                            }

                            // =========================
                            // DATE CHECK
                            // =========================
                            String date = order.getDate();

                            if (date != null) {

                                // contoh:
                                // 16 May 2026 12:30
                                String orderDate =
                                        date.length() >= 11
                                                ? date.substring(0, 11)
                                                : "";

                                // =====================
                                // TODAY
                                // =====================
                                if (orderDate.equals(today)) {

                                    todayOrders++;

                                    if ("Done".equals(status)) {
                                        todayRevenue += order.getTotal();
                                    }
                                }

                                // =====================
                                // YESTERDAY
                                // =====================
                                if (orderDate.equals(yesterday)) {

                                    yesterdayOrders++;

                                    if ("Done".equals(status)) {
                                        yesterdayRevenue += order.getTotal();
                                    }
                                }
                            }
                        }
                    }
                }

                Collections.reverse(orderList);

                adapter.notifyDataSetChanged();

                // =========================
                // SET MAIN DATA
                // =========================
                tvTotalOrders.setText(
                        String.valueOf(orderList.size())
                );

                tvPendingOrders.setText(
                        String.valueOf(pending)
                );

                tvProcessOrders.setText(
                        String.valueOf(process)
                );

                tvSuccessOrders.setText(
                        String.valueOf(success)
                );

                tvDailyRevenue.setText(
                        "Rp " + String.format("%,.0f", revenue)
                );

                // =========================
                // ORDER GROWTH
                // =========================
                int orderDiff =
                        todayOrders - yesterdayOrders;

                String orderGrowthText;

                if (orderDiff > 0) {

                    orderGrowthText =
                            "+" + orderDiff + " from yesterday";

                } else if (orderDiff < 0) {

                    orderGrowthText =
                            orderDiff + " from yesterday";

                } else {

                    orderGrowthText =
                            "Same as yesterday";
                }

                tvOrderGrowth.setText(orderGrowthText);

                // =========================
                // REVENUE GROWTH %
                // =========================
                String revenueGrowthText;

                if (yesterdayRevenue > 0) {

                    double percent =
                            ((todayRevenue - yesterdayRevenue)
                                    / yesterdayRevenue) * 100;

                    revenueGrowthText =
                            String.format(
                                    java.util.Locale.getDefault(),
                                    "%+.0f%% from yesterday",
                                    percent
                            );

                } else {

                    revenueGrowthText =
                            "No revenue yesterday";
                }

                tvRevenueGrowth.setText(revenueGrowthText);
            }

            @Override
            public void onCancelled(DatabaseError error) {

                Toast.makeText(
                        HomeAdminActivity.this,
                        error.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    // ================================
    // EXPORT PDF REPORT
    // ================================
    private void exportPdfReport() {

        PdfDocument pdfDocument = new PdfDocument();

        Paint titlePaint = new Paint();
        Paint textPaint = new Paint();

        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(
                        1200,
                        2010,
                        1
                ).create();

        PdfDocument.Page page =
                pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();

        // TITLE
        titlePaint.setTextSize(42);
        titlePaint.setFakeBoldText(true);

        canvas.drawText(
                "CAMPEAT SALES REPORT",
                320,
                100,
                titlePaint
        );

        // SUBTITLE
        textPaint.setTextSize(24);

        canvas.drawText(
                "Admin Revenue Report",
                450,
                150,
                textPaint
        );

        // TABLE HEADER
        titlePaint.setTextSize(28);

        int startY = 240;

        canvas.drawText(
                "Customer",
                80,
                startY,
                titlePaint
        );

        canvas.drawText(
                "Payment",
                380,
                startY,
                titlePaint
        );

        canvas.drawText(
                "Status",
                650,
                startY,
                titlePaint
        );

        canvas.drawText(
                "Total",
                900,
                startY,
                titlePaint
        );

        // ORDER LIST
        textPaint.setTextSize(24);

        int y = 300;

        double totalRevenue = 0;

        for (Order order : orderList) {

            canvas.drawText(
                    order.getCustomerName(),
                    80,
                    y,
                    textPaint
            );

            canvas.drawText(
                    order.getPayment(),
                    380,
                    y,
                    textPaint
            );

            canvas.drawText(
                    order.getStatus(),
                    650,
                    y,
                    textPaint
            );

            canvas.drawText(
                    "Rp " + String.format("%,.0f", (double) order.getTotal()),
                    900,
                    y,
                    textPaint
            );

            y += 50;

            if ("Done".equals(order.getStatus())) {

                totalRevenue += order.getTotal();
            }
        }

        // TOTAL REVENUE
        titlePaint.setTextSize(34);

        canvas.drawText(
                "TOTAL REVENUE : Rp " +
                        String.format("%,.0f", totalRevenue),
                80,
                y + 100,
                titlePaint
        );

        pdfDocument.finishPage(page);

        // SAVE PDF
        File file = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                ),
                "Campeat_Report.pdf"
        );

        try {

            pdfDocument.writeTo(
                    new FileOutputStream(file)
            );

            Toast.makeText(
                    this,
                    "PDF berhasil disimpan di Downloads",
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception e) {

            e.printStackTrace();

            Toast.makeText(
                    this,
                    "Gagal export PDF",
                    Toast.LENGTH_SHORT
            ).show();
        }

        pdfDocument.close();
    }
}