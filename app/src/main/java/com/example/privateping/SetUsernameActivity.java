package com.example.privateping;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SetUsernameActivity extends AppCompatActivity {

    EditText usernameInput;
    Button submitBtn;
    FirebaseAuth mAuth;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_username);

        usernameInput = findViewById(R.id.usernameInput);
        submitBtn = findViewById(R.id.submitBtn);
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        submitBtn.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            if (username.isEmpty()) {
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
                return;
            }

            // Append unique suffix (e.g., hash of email)
            String email = mAuth.getCurrentUser().getEmail();
            String uniqueSuffix = Integer.toHexString(email.hashCode()).substring(0, 4);
            String finalUsername = username + "_" + uniqueSuffix;

            String uid = mAuth.getCurrentUser().getUid();
            usersRef.child(uid).child("username").setValue(finalUsername)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Username set: " + finalUsername, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, JoinRoomActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to set username: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
}