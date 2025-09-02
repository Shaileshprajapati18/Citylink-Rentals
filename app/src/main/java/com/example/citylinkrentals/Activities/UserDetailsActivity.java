package com.example.citylinkrentals.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.citylinkrentals.model.User;
import com.example.citylinkrentals.R;
import com.example.citylinkrentals.network.ApiService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserDetailsActivity extends AppCompatActivity {

    private EditText usernameBox, emailBox, phoneBox;
    private Button continueBtn;
    private ApiService apiService;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_details);

        usernameBox = findViewById(R.id.usernameBox);
        emailBox = findViewById(R.id.emailBox);
        phoneBox = findViewById(R.id.phoneBox);
        continueBtn = findViewById(R.id.continueBtn);

        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.153.1:8082/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        getWindow().setStatusBarColor(getResources().getColor(R.color.main_color));

        continueBtn.setOnClickListener(v -> saveUserDetails());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && firebaseUser.getPhoneNumber() != null) {
            phoneBox.setText(firebaseUser.getPhoneNumber());
        }
    }

    private void saveUserDetails() {
        String username = usernameBox.getText().toString().trim();
        String email = emailBox.getText().toString().trim();
        String phoneNumber = phoneBox.getText().toString().trim();

        // Input validation
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

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String firebaseUid = firebaseUser != null ? firebaseUser.getUid() : "null";

        User user = new User(firebaseUid, username, email, phoneNumber);

        Call<User> call = apiService.postUser(user);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(UserDetailsActivity.this, "User details saved successfully!", Toast.LENGTH_SHORT).show();
                    // Mark user details as saved in SharedPreferences
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("isUserDetailsSaved_" + firebaseUid, true);
                    editor.apply();

                    Intent intent = new Intent(UserDetailsActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(UserDetailsActivity.this, "Failed to save user details: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(UserDetailsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}