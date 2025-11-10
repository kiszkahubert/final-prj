package com.kiszka.kiddify;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kiszka.kiddify.adapters.ChatMessageAdapter;
import com.kiszka.kiddify.database.DataManager;
import com.kiszka.kiddify.databinding.ActivityChatBinding;
import com.kiszka.kiddify.models.ChatMessageDTO;
import com.kiszka.kiddify.models.Message;
import com.kiszka.kiddify.models.PeopleInfo;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tech.gusavila92.websocketclient.WebSocketClient;

public class ChatActivity extends AppCompatActivity {
    private ActivityChatBinding binding;
    private ChatMessageAdapter chatAdapter;
    private WebSocketClient webSocketClient;
    private DataManager dataManager;
    private int myKidId;
    private Gson gson;
    private boolean isConnected = false;
    private final okhttp3.OkHttpClient httpClient = new okhttp3.OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        this.binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        initializeComponents();
        setupRecyclerView();
        setupClickListeners();
        View root = binding.getRoot();
        final View recycler = binding.messagesRecyclerView;
        final View inputLayout = binding.messageInputLayout;
        final int originalRecyclerBottom = recycler.getPaddingBottom();
        // code below handles window insets from the on-screen keyboard to keep the UI visible
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            int imeBottom = imeInsets.bottom;
            if (imeBottom > 0) {
                recycler.setPadding(recycler.getPaddingLeft(), recycler.getPaddingTop(), recycler.getPaddingRight(), imeBottom);
                inputLayout.setTranslationY(-imeBottom);
            } else {
                recycler.setPadding(recycler.getPaddingLeft(), recycler.getPaddingTop(), recycler.getPaddingRight(), originalRecyclerBottom);
                inputLayout.setTranslationY(0);
            }
            return insets;
        });
        String formattedDate = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", new Locale("en","US"));
            formattedDate = today.format(formatter);
        }
        binding.tvTitle.setText("Family Chat");
        binding.tvCurrentDate.setText(formattedDate);
        connectWebSocket();
        fetchFamilyPeople();
        observeMessages();
        dataSetup();
    }
    private void initializeComponents() {
        dataManager = DataManager.getInstance(this);
        myKidId = dataManager.getKidId();
        gson = new Gson();
    }
    private void setupRecyclerView() {
        chatAdapter = new ChatMessageAdapter(this, myKidId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.messagesRecyclerView.setLayoutManager(layoutManager);
        binding.messagesRecyclerView.setAdapter(chatAdapter);
    }
    private void setupClickListeners() {
        binding.sendButton.setOnClickListener(v -> sendMessage());
        binding.btnBack.setOnClickListener(v -> finish());
        binding.messageEditText.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
        binding.messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = s != null && !s.toString().trim().isEmpty();
                binding.sendButton.setEnabled(hasText);
                int primaryColor = ContextCompat.getColor(ChatActivity.this, R.color.primary_blue);
                int grayColor = ContextCompat.getColor(ChatActivity.this, R.color.button_disabled_gray);
                binding.sendButton.setBackgroundTintList(ColorStateList.valueOf(hasText ? primaryColor : grayColor));
                binding.sendButton.setImageTintList(ColorStateList.valueOf(Color.WHITE));
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    private void connectWebSocket() {
        try {
            URI uri = URI.create("ws://10.0.2.2:8080/chat-websocket/websocket");
            String token = dataManager.getToken();
            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen() {
                    Log.d("WebSocket", "Connected to server");
                    isConnected = true;
                    subscribeToTopic();
                }
                @Override
                public void onTextReceived(String message) {
                    Log.d("WebSocket", "Received: " + message);
                    handleIncomingMessage(message);
                }
                @Override
                public void onBinaryReceived(byte[] data) {}
                @Override
                public void onPingReceived(byte[] data) {}
                @Override
                public void onPongReceived(byte[] data) {}
                @Override
                public void onException(Exception e) {
                    Log.e("WebSocket", "Exception: " + e.getMessage());
                    isConnected = false;
                }
                @Override
                public void onCloseReceived() {
                    Log.d("WebSocket", "Connection closed");
                    isConnected = false;
                }
            };
            if (token != null && !token.isEmpty()) {
                webSocketClient.addHeader("Authorization", "Bearer " + token);
            }
            webSocketClient.connect();
        } catch (Exception e) {
            Log.e("WebSocket", "Failed to connect: " + e.getMessage());
            isConnected = false;
        }
    }
    // opens a STOMP WebSocket session
    private void subscribeToTopic() {
        String token = dataManager.getToken();
        String connectFrame = "CONNECT\n" +
                "accept-version:1.1,1.0\n" +
                "heart-beat:10000,10000\n" +
                (token != null ? "Authorization:Bearer " + token + "\n" : "") +
                "\n" +
                "\u0000";
        webSocketClient.send(connectFrame);
        String subscribeFrame = "SUBSCRIBE\n" +
                "id:sub-0\n" +
                "destination:/topic/familyChat\n" +
                "\n" +
                "\u0000";
        webSocketClient.send(subscribeFrame);
    }
    // handles incoming messages from the server
    private void handleIncomingMessage(String rawMessage) {
        try {
            if (rawMessage.startsWith("MESSAGE")) {
                String[] lines = rawMessage.split("\n");
                String jsonPayload = "";
                boolean foundBody = false;
                for (String line : lines) {
                    if (foundBody) {
                        jsonPayload = line.replace("\u0000", "");
                        break;
                    }
                    if (line.isEmpty()) {
                        foundBody = true;
                    }
                }
                if (!jsonPayload.isEmpty()) {
                    ChatMessageDTO dto = gson.fromJson(jsonPayload, ChatMessageDTO.class);
                    if (dto.getSenderType().equals("KID") && dto.getSenderId() == myKidId) {
                        return;
                    }
                    Message message = new Message();
                    message.setSenderType(dto.getSenderType());
                    message.setSenderId(dto.getSenderId());
                    message.setContent(dto.getContent());
                    message.setSentAt(dto.getSentAt());
                    dataManager.saveMessage(message);
                }
            }
        } catch (Exception e) {
            Log.e("WebSocket", "Error parsing message: " + e.getMessage());
        }
    }
    private void sendMessage() {
        String messageText = binding.messageEditText.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            return;
        }
        if (!isConnected) {
            Toast.makeText(this, "Not connected to server", Toast.LENGTH_SHORT).show();
            return;
        }
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setSenderType("KID");
        dto.setSenderId(myKidId);
        dto.setContent(messageText);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dto.setSentAt(LocalDateTime.now().toString());
        }
        sendMessageViaWebSocket(dto);
        Message localMessage = new Message();
        localMessage.setSenderType("KID");
        localMessage.setSenderId(myKidId);
        localMessage.setContent(messageText);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            localMessage.setSentAt(LocalDateTime.now().toString());
        }
        dataManager.saveMessage(localMessage);
        binding.messageEditText.setText("");
    }
    private void sendMessageViaWebSocket(ChatMessageDTO dto) {
        try {
            String jsonPayload = gson.toJson(dto);
            String sendFrame = "SEND\n" +
                    "destination:/app/sendMessage\n" +
                    "content-type:application/json\n" +
                    "\n" +
                    jsonPayload +
                    "\u0000";
            webSocketClient.send(sendFrame);
            Log.d("WebSocket", "Message sent: " + jsonPayload);
        } catch (Exception e) {
            Log.e("WebSocket", "Failed to send message: " + e.getMessage());
            runOnUiThread(() -> Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show());
        }
    }
    private void observeMessages() {
        dataManager.getAllMessages().observe(this, messages -> {
            if (messages != null) {
                chatAdapter.setMessages(messages);
                if (!messages.isEmpty()) {
                    binding.messagesRecyclerView.scrollToPosition(messages.size() - 1);
                }
                if (messages.isEmpty()) {
                    binding.emptyStateText.setVisibility(View.VISIBLE);
                    binding.messagesRecyclerView.setVisibility(View.GONE);
                } else {
                    binding.emptyStateText.setVisibility(View.GONE);
                    binding.messagesRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    private void fetchFamilyPeople() {
        String url = "http://10.0.2.2:8080/api/family/people";
        String token = dataManager.getToken();
        Request.Builder reqBuilder = new Request.Builder().url(url).get();
        if (token != null && !token.isEmpty()) {
            reqBuilder.addHeader("Authorization", "Bearer " + token);
        }
        httpClient.newCall(reqBuilder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("ChatActivity", "Failed to fetch people: " + e.getMessage());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("ChatActivity", "Unexpected response fetching people: " + response.code());
                    return;
                }
                String body = response.body().string();
                try {
                    PeopleInfo[] people = gson.fromJson(body, PeopleInfo[].class);
                    Map<String, String> map = new HashMap<>();
                    if (people != null) {
                        for (PeopleInfo p : people) {
                            if (p == null) continue;
                            String key = p.getType() + ":" + p.getId();
                            map.put(key, p.getName());
                        }
                    }
                    runOnUiThread(() -> chatAdapter.setPeopleMap(map));
                } catch (Exception e) {
                    Log.e("ChatActivity", "Error parsing people: " + e.getMessage());
                }
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (!isConnected) {
            connectWebSocket();
        }
    }
    private void dataSetup() {
        String token = DataManager.getInstance(this).getToken();
        OkHttpClient client = new OkHttpClient();
        Request requestMessages = new Request.Builder()
                .url("http://10.0.2.2:8080/api/chat/messages")
                .addHeader("Authorization", "Bearer " + token)
                .build();
        client.newCall(requestMessages).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("API", "Messages failed: " + e.getMessage());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Type listType = new TypeToken<List<Message>>(){}.getType();
                    List<Message> messages = new Gson().fromJson(body, listType);
                    DataManager.getInstance(ChatActivity.this).saveMessages(messages);
                    Log.d("API", "Messages saved: " + messages.size());
                }
            }
        });
    }
}