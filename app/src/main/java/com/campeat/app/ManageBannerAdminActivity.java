package com.campeat.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.campeat.app.adapter.BannerAdminAdapter;
import com.campeat.app.model.BannerModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ManageBannerAdminActivity extends AppCompatActivity {

    private RecyclerView rvBanner;
    private FloatingActionButton fabAddBanner;

    private List<BannerModel> bannerList;
    private BannerAdminAdapter adapter;

    private DatabaseReference bannerRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_banner_admin);

        getWindow().setStatusBarColor(Color.parseColor("#EEF8F2"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        initViews();
        setupRecyclerView();
        loadBanner();

        fabAddBanner.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            this,
                            AddBannerAdminActivity.class
                    )
            );
        });
    }

    private void initViews() {

        rvBanner = findViewById(R.id.rv_banner);

        fabAddBanner =
                findViewById(R.id.fab_add_banner);

        bannerList = new ArrayList<>();

        bannerRef = FirebaseDatabase.getInstance(
                ""
        ).getReference("banners");
    }

    private void setupRecyclerView() {

        adapter =
                new BannerAdminAdapter(this, bannerList);

        rvBanner.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rvBanner.setAdapter(adapter);
    }

    private void loadBanner() {

        bannerRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                bannerList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {

                    BannerModel banner =
                            data.getValue(BannerModel.class);

                    if (banner != null) {

                        banner.setKey(data.getKey());

                        bannerList.add(banner);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }
}