package com.campeat.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.campeat.app.adapter.ChatAdapter;
import com.campeat.app.model.ChatMessage;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChatbotActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private EditText etMessage;
    private ImageView btnSend, btnBack;

    private List<ChatMessage> messages;
    private ChatAdapter adapter;

    private GenerativeModelFutures modelFutures;
    private final Executor executor = Executors.newSingleThreadExecutor();

    private String uid;
    private String userName = "User";
    private String menuContext = "Belum ada data menu.";
    private String orderContext = "Tidak ada pesanan aktif.";
    private boolean isSending = false;

    private static final String GEMINI_API_KEY = "";
    private static final String DB_URL =
            "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        initViews();
        setupRecyclerView();
        initGemini();
        loadUserContext();
        loadMenuContext();

        btnBack.setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void initViews() {
        rvChat = findViewById(R.id.rv_chat);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        btnBack = findViewById(R.id.btn_back);
    }

    private void setupRecyclerView() {
        messages = new ArrayList<>();
        adapter = new ChatAdapter(messages);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);
    }

    private void initGemini() {
        GenerativeModel model = new GenerativeModel(
                "gemini-2.5-flash-lite",
                GEMINI_API_KEY
        );

        modelFutures = GenerativeModelFutures.from(model);

        addBotMessage("Selamat datang di CampEats! Saya Campeat Assistant. Ada yang bisa saya bantu hari ini?");
    }

    private void loadUserContext() {
        if (uid == null) return;

        FirebaseDatabase.getInstance(DB_URL)
                .getReference("users")
                .child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String name = snapshot.child("name").getValue(String.class);
                        if (name != null && !name.isEmpty()) {
                            userName = name;
                        }

                        loadLatestOrders();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e("CHATBOT_FIREBASE", "Gagal load user", error.toException());
                    }
                });
    }

    private void loadMenuContext() {
        FirebaseDatabase.getInstance(DB_URL)
                .getReference("menus")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        StringBuilder sb = new StringBuilder();

                        for (DataSnapshot menuSnap : snapshot.getChildren()) {
                            Boolean archived = menuSnap.child("archived").getValue(Boolean.class);
                            if (archived != null && archived) continue;

                            String name = menuSnap.child("name").getValue(String.class);
                            String category = menuSnap.child("category").getValue(String.class);
                            Integer price = menuSnap.child("price").getValue(Integer.class);
                            Integer stock = menuSnap.child("stock").getValue(Integer.class);
                            Object rating = menuSnap.child("rating").getValue();

                            if (name == null || name.trim().isEmpty()) continue;

                            sb.append("- ")
                                    .append(name)
                                    .append(" | Kategori: ").append(category != null ? category : "-")
                                    .append(" | Harga: Rp ").append(price != null ? price : 0)
                                    .append(" | Stok: ").append(stock != null ? stock : 0)
                                    .append(" | Rating: ").append(rating != null ? rating : "-")
                                    .append("\n");
                        }

                        menuContext = sb.length() > 0
                                ? sb.toString()
                                : "Belum ada menu aktif.";
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        menuContext = "Gagal memuat data menu.";
                        Log.e("CHATBOT_FIREBASE", "Gagal load menu", error.toException());
                    }
                });
    }

    private void loadLatestOrders() {
        if (uid == null) return;

        FirebaseDatabase.getInstance(DB_URL)
                .getReference("orders")
                .child(uid)
                .limitToLast(3)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        StringBuilder sb = new StringBuilder();

                        for (DataSnapshot orderSnap : snapshot.getChildren()) {
                            String orderId = orderSnap.getKey();
                            String status = orderSnap.child("status").getValue(String.class);
                            String date = orderSnap.child("date").getValue(String.class);
                            Double total = orderSnap.child("total").getValue(Double.class);

                            String firstItem = "";
                            for (DataSnapshot itemSnap : orderSnap.child("items").getChildren()) {
                                String name = itemSnap.child("name").getValue(String.class);
                                if (name != null) {
                                    firstItem = name;
                                    break;
                                }
                            }

                            sb.append("Order ID: ").append(orderId != null ? orderId : "-")
                                    .append(", Status: ").append(status != null ? status : "-")
                                    .append(", Item: ").append(!firstItem.isEmpty() ? firstItem : "-")
                                    .append(", Total: Rp ").append(total != null ? total.intValue() : 0)
                                    .append(", Tanggal: ").append(date != null ? date : "-")
                                    .append("\n");
                        }

                        orderContext = sb.length() > 0
                                ? sb.toString()
                                : "Tidak ada pesanan aktif.";
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        orderContext = "Gagal memuat data pesanan.";
                        Log.e("CHATBOT_FIREBASE", "Gagal load order", error.toException());
                    }
                });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        if (isSending) {
            addBotMessage("Tunggu sebentar ya, aku masih menjawab pertanyaan sebelumnya.");
            return;
        }

        isSending = true;
        btnSend.setEnabled(false);
        btnSend.setAlpha(0.5f);

        addUserMessage(text);
        etMessage.setText("");

        String fullPrompt =
                "Kamu adalah Campeat Assistant, asisten virtual kantin kampus CampEats.\n" +
                        "Jawab dalam Bahasa Indonesia yang ramah, singkat, jelas, dan jangan mengarang data.\n" +
                        "Jika user bertanya menu, rekomendasikan hanya dari DATA MENU AKTIF.\n" +
                        "Jika menu stoknya 0, bilang menu sedang tidak tersedia.\n" +
                        "Jika user bertanya pesanan, gunakan DATA PESANAN TERBARU.\n" +
                        "Jika data yang ditanya tidak tersedia, bilang datanya belum tersedia di aplikasi.\n\n" +
                        "Nama pengguna: " + userName + "\n\n" +
                        "DATA MENU AKTIF:\n" + menuContext + "\n" +
                        "DATA PESANAN TERBARU:\n" + orderContext + "\n" +
                        "PERTANYAAN USER:\n" + text;

        Content.Builder builder = new Content.Builder();
        builder.setRole("user");
        builder.addText(fullPrompt);
        Content content = builder.build();

        addBotMessage("...");
        int typingIndex = messages.size() - 1;

        ListenableFuture<GenerateContentResponse> response =
                modelFutures.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String reply = result.getText();

                runOnUiThread(() -> {
                    removeTypingMessage(typingIndex);
                    unlockSendButton();

                    addBotMessage(reply != null && !reply.trim().isEmpty()
                            ? reply.trim()
                            : "Maaf, saya belum bisa memahami pertanyaan itu.");

                    if (isAskingAboutOrder(text)) {
                        showOrderCard();
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("CHATBOT_ERROR", "Gemini error", t);

                runOnUiThread(() -> {
                    removeTypingMessage(typingIndex);
                    unlockSendButton();
                    addBotMessage(getFriendlyErrorMessage(t));
                });
            }
        }, executor);
    }

    private void removeTypingMessage(int typingIndex) {
        if (typingIndex >= 0 && typingIndex < messages.size()) {
            messages.remove(typingIndex);
            adapter.notifyItemRemoved(typingIndex);
        }
    }

    private void unlockSendButton() {
        isSending = false;
        btnSend.setEnabled(true);
        btnSend.setAlpha(1f);
    }

    private String getFriendlyErrorMessage(Throwable t) {
        String message = t.getMessage() != null ? t.getMessage().toLowerCase() : "";

        if (message.contains("quota") || message.contains("rate")) {
            return "Kuota AI sedang penuh. Coba lagi sebentar ya.";
        }

        if (message.contains("api key") || message.contains("permission") || message.contains("unauthenticated")) {
            return "Konfigurasi AI belum valid. Cek API key dulu ya.";
        }

        if (message.contains("not found") || message.contains("model")) {
            return "Model AI belum tersedia untuk project ini. Cek nama model atau quota Gemini.";
        }

        return "Maaf, terjadi kesalahan saat menghubungi AI. Coba lagi ya.";
    }

    private boolean isAskingAboutOrder(String text) {
        String lower = text.toLowerCase();
        return lower.contains("order") ||
                lower.contains("pesanan") ||
                lower.contains("status") ||
                lower.contains("delivery") ||
                lower.contains("pesan") ||
                lower.contains("mana makanan");
    }

    private void showOrderCard() {
        if (uid == null) return;

        FirebaseDatabase.getInstance(DB_URL)
                .getReference("orders")
                .child(uid)
                .limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot orderSnap : snapshot.getChildren()) {
                            String orderId = orderSnap.getKey();
                            String status = orderSnap.child("status").getValue(String.class);

                            String shortId = orderId != null && orderId.length() > 6
                                    ? "#CE-" + orderId.substring(orderId.length() - 6).toUpperCase()
                                    : "#CE-000000";

                            String foodName = "Menu";
                            String imageBase64 = "";

                            for (DataSnapshot itemSnap : orderSnap.child("items").getChildren()) {
                                String name = itemSnap.child("name").getValue(String.class);
                                String img = itemSnap.child("imageBase64").getValue(String.class);

                                if (name != null) foodName = name;
                                if (img != null) imageBase64 = img;
                                break;
                            }

                            String estimated = "15 Minutes";
                            if (status != null) {
                                switch (status) {
                                    case "Pending":
                                        estimated = "15 Minutes";
                                        break;
                                    case "Process":
                                        estimated = "10 Minutes";
                                        break;
                                    case "Ready":
                                        estimated = "2 Minutes";
                                        break;
                                    case "Done":
                                    case "Success":
                                        estimated = "Completed";
                                        break;
                                }
                            }

                            final String finalStatus = status != null ? status : "Pending";
                            final String finalFoodName = foodName;
                            final String finalImageBase64 = imageBase64;
                            final String finalEstimated = estimated;

                            runOnUiThread(() -> {
                                messages.add(new ChatMessage(
                                        shortId,
                                        finalStatus,
                                        finalFoodName,
                                        finalEstimated,
                                        finalImageBase64,
                                        getCurrentTime()
                                ));
                                adapter.notifyItemInserted(messages.size() - 1);
                                scrollToBottom();
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e("CHATBOT_FIREBASE", "Gagal show order card", error.toException());
                    }
                });
    }

    private void addBotMessage(String text) {
        messages.add(new ChatMessage(text, ChatMessage.TYPE_BOT, getCurrentTime()));
        adapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }

    private void addUserMessage(String text) {
        messages.add(new ChatMessage(text, ChatMessage.TYPE_USER, getCurrentTime()));
        adapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }

    private void scrollToBottom() {
        if (!messages.isEmpty()) {
            rvChat.smoothScrollToPosition(messages.size() - 1);
        }
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
    }
}