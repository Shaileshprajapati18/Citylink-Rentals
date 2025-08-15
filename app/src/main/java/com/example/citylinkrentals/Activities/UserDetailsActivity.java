package com.example.citylinkrentals.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.citylinkrentals.MainActivity;
import com.example.citylinkrentals.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class UserDetailsActivity extends AppCompatActivity {

    private EditText usernameBox, emailBox, phoneBox;
    private Button continueBtn;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_details);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        usernameBox = findViewById(R.id.usernameBox);
        emailBox = findViewById(R.id.emailBox);
        phoneBox = findViewById(R.id.phoneBox);
        continueBtn = findViewById(R.id.continueBtn);

        getWindow().setStatusBarColor(getResources().getColor(R.color.main_color));

        continueBtn.setOnClickListener(v -> saveUserDetails());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void saveUserDetails() {

        String username = usernameBox.getText().toString().trim();
        String email = emailBox.getText().toString().trim();
        String phoneNumber = phoneBox.getText().toString().trim();

        if (username.isEmpty()) {
            usernameBox.setError("Username is required");
            usernameBox.requestFocus();
            return;
        }
        if (username.length() < 3) {
            usernameBox.setError("Username must be at least 3 characters long");
            usernameBox.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            emailBox.setError("Email is required");
            emailBox.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailBox.setError("Enter a valid email address");
            emailBox.requestFocus();
            return;
        }

        if (phoneNumber.isEmpty()) {
            phoneBox.setError("Phone number is required");
            phoneBox.requestFocus();
            return;
        }
        if (!phoneNumber.startsWith("+") || phoneNumber.length() < 10 || phoneNumber.length() > 13) {
            phoneBox.setError("Enter a valid phone number with country code (e.g., +91xxxxxxxxxx)");
            phoneBox.requestFocus();
            return;
        }

        // Check if user is authenticated
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated. Please sign in again.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(UserDetailsActivity.this, LoginActivity.class));
            finish();
            return;
        }

        String userId = user.getUid();
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("username", username);
        userDetails.put("email", email);
        userDetails.put("phoneNumber", phoneNumber);

        mDatabase.child("users").child(userId).setValue(userDetails)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UserDetailsActivity.this, "User details saved successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(UserDetailsActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UserDetailsActivity.this, "Failed to save user details: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}