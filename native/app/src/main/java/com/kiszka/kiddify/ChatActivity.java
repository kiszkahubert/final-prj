package com.kiszka.kiddify;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.kiszka.kiddify.adapters.ChatMessageAdapter;
import com.kiszka.kiddify.database.DataManager;
import com.kiszka.kiddify.databinding.ActivityChatBinding;
import com.kiszka.kiddify.models.ChatMessageDTO;
import com.kiszka.kiddify.models.Message;

import java.net.URI;
import java.time.LocalDateTime;

import tech.gusavila92.websocketclient.WebSocketClient;

public class ChatActivity extends AppCompatActivity {
    private ActivityChatBinding binding;
    private ChatMessageAdapter chatAdapter;
    private WebSocketClient webSocketClient;
    private DataManager dataManager;
    private int myKidId;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        this.binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeComponents();
        setupRecyclerView();
        setupClickListeners();
        connectWebSocket();
        observeMessages();
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
    }
    private void connectWebSocket() {
        try {
            URI uri = URI.create("ws://10.0.2.2:8080/chat-websocket/websocket");
            String token = dataManager.getToken();
            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen() {
                    Log.d("WebSocket", "Connected to server");
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
                }
                @Override
                public void onCloseReceived() {
                    Log.d("WebSocket", "Connection closed");
                }
            };
            if (token != null && !token.isEmpty()) {
                webSocketClient.addHeader("Authorization", "Bearer " + token);
            }
            webSocketClient.connect();
        } catch (Exception e) {
            Log.e("WebSocket", "Failed to connect: " + e.getMessage());
        }
    }
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
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setSenderType("KID");
        dto.setSenderId(myKidId);
        dto.setContent(messageText);
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
        } catch (Exception e) {
            Log.e("WebSocket", "Failed to send message: " + e.getMessage());
            runOnUiThread(() -> Toast.makeText(this, "Nie udało się wysłać wiadomości", Toast.LENGTH_SHORT).show());
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
        if (webSocketClient != null) {
            connectWebSocket();
        }
    }
}