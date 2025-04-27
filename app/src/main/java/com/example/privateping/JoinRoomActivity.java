package com.example.privateping;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.HashMap;

public class JoinRoomActivity extends AppCompatActivity {

    EditText roomIdInput, secretCodeInput;
    Button joinBtn;
    DatabaseReference databaseRef;
    FirebaseAuth mAuth;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_room);

        roomIdInput = findViewById(R.id.roomIdInput);
        secretCodeInput = findViewById(R.id.secretCodeInput);
        joinBtn = findViewById(R.id.joinBtn);
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("rooms");

        // Fetch username from Firebase
        String uid = mAuth.getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("users").child(uid).child("username")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            username = snapshot.getValue(String.class);
                        } else {
                            Toast.makeText(JoinRoomActivity.this, "Username not found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(JoinRoomActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        joinBtn.setOnClickListener(v -> {
            String roomId = roomIdInput.getText().toString().trim();
            String secret = secretCodeInput.getText().toString().trim();

            if (username == null || roomId.isEmpty() || secret.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences.Editor editor = getSharedPreferences("room_auth", MODE_PRIVATE).edit();
            editor.putString("username", username);
            editor.putString("roomId", roomId);
            editor.putString("secret", secret);
            editor.putLong("joinTimestamp", System.currentTimeMillis());
            editor.apply();

            databaseRef.child(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("createdAt", System.currentTimeMillis());
                        databaseRef.child(roomId).setValue(map);
                        Toast.makeText(JoinRoomActivity.this, "Room created", Toast.LENGTH_SHORT).show();
                    }
                    startActivity(new Intent(JoinRoomActivity.this, ChatActivity.class));
                    finish();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(JoinRoomActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}