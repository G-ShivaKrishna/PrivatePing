package com.example.privateping;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import androidx.appcompat.app.AppCompatDelegate;
import java.util.ArrayList;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;
    private static final String TAG = "ChatActivity";

    private ListView listView;
    private ChatAdapter chatAdapter;
    private ArrayList<ChatMessage> messageList;
    private EditText messageInput;
    private MaterialButton sendBtn, attachBtn;
    private TextView roomCodeText;
    private Button clearChatButton;
    private DatabaseReference chatRef;
    private StorageReference storageRef;
    private String username;
    private String roomId;
    private long joinTimestamp;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        listView = findViewById(R.id.listView);
        messageInput = findViewById(R.id.messageInput);
        sendBtn = findViewById(R.id.sendBtn);
        attachBtn = findViewById(R.id.attachBtn);
        roomCodeText = findViewById(R.id.roomCodeText);
        clearChatButton = findViewById(R.id.clear_chat_button);

        SharedPreferences roomPrefs = getSharedPreferences("room_auth", MODE_PRIVATE);
        roomId = roomPrefs.getString("roomId", null);
        joinTimestamp = roomPrefs.getLong("joinTimestamp", 0);

        if (roomId == null || joinTimestamp == 0) {
            Log.e(TAG, "Missing roomId or joinTimestamp");
            Toast.makeText(this, "Invalid session. Please rejoin.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, JoinRoomActivity.class));
            finish();
            return;
        }

        // Fetch username from Firebase
        String uid = mAuth.getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("users").child(uid).child("username")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            username = snapshot.getValue(String.class);
                            initializeChat();
                        } else {
                            Log.e(TAG, "Username not found for uid: " + uid);
                            Toast.makeText(ChatActivity.this, "Username not found", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(ChatActivity.this, JoinRoomActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to fetch username: " + error.getMessage());
                        Toast.makeText(ChatActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initializeChat() {
        roomCodeText.setText("Room Code: " + roomId);
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messageList);
        listView.setAdapter(chatAdapter);

        chatRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomId).child("messages");
        storageRef = FirebaseStorage.getInstance().getReference();

        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomId);
        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.e(TAG, "Room does not exist: " + roomId);
                    Toast.makeText(ChatActivity.this, "Room not found. Please rejoin.", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(ChatActivity.this, JoinRoomActivity.class));
                    finish();
                } else {
                    loadMessages();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Room check failed: " + error.getMessage());
                Toast.makeText(ChatActivity.this, "Error checking room: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        sendBtn.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString().trim();
            if (!messageText.isEmpty()) {
                ChatMessage chatMessage = new ChatMessage(messageText, username, System.currentTimeMillis());
                String messageId = chatRef.push().getKey();
                chatRef.child(messageId).setValue(chatMessage)
                        .addOnSuccessListener(aVoid -> {
                            messageInput.setText("");
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(ChatActivity.this, "Cannot send message: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }
        });

        attachBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST);
        });

        clearChatButton.setOnClickListener(v -> clearChat());
    }

    private void loadMessages() {
        Query messageQuery = chatRef.orderByChild("timestamp").startAt(joinTimestamp);
        messageQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    ChatMessage message = messageSnapshot.getValue(ChatMessage.class);
                    if (message != null && message.getTimestamp() >= joinTimestamp) {
                        messageList.add(message);
                    }
                }
                chatAdapter.notifyDataSetChanged();
                if (!messageList.isEmpty()) {
                    listView.setSelection(messageList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Error loading messages: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        chatRef.orderByChild("timestamp").startAt(joinTimestamp).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ChatMessage message = snapshot.getValue(ChatMessage.class);
                if (message != null && message.getTimestamp() >= joinTimestamp) {
                    boolean exists = false;
                    for (ChatMessage existing : messageList) {
                        if (existing.getMessage().equals(message.getMessage()) &&
                                existing.getUser().equals(message.getUser()) &&
                                existing.getTimestamp() == message.getTimestamp()) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        messageList.add(message);
                        chatAdapter.notifyDataSetChanged();
                        listView.setSelection(messageList.size() - 1);
                    }
                }
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void clearChat() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to clear the chat?")
                .setCancelable(false)
                .setPositiveButton("Clear", (dialog, which) -> {
                    chatRef.removeValue()
                            .addOnSuccessListener(aVoid -> {
                                messageList.clear();
                                chatAdapter.notifyDataSetChanged();
                                Toast.makeText(this, "Chat cleared", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to clear chat: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri fileUri = data.getData();
            String fileName = UUID.randomUUID().toString();
            StorageReference fileRef = storageRef.child("chat_files").child(roomId).child(fileName);

            UploadTask uploadTask = fileRef.putFile(fileUri);
            uploadTask.addOnSuccessListener(taskSnapshot ->
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String fileUrl = uri.toString();
                        ChatMessage fileMessage = new ChatMessage("File: " + fileUrl, username, System.currentTimeMillis());
                        String messageId = chatRef.push().getKey();
                        chatRef.child(messageId).setValue(fileMessage);
                    })
            ).addOnFailureListener(e -> {
                Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }
}