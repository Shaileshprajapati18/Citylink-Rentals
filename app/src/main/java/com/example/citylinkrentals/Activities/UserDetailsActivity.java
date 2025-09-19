package com.example.citylinkrentals.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.citylinkrentals.R;
import com.example.citylinkrentals.model.User;
import com.example.citylinkrentals.network.ApiService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserDetailsActivity extends AppCompatActivity {

    private static final String TAG = "UserDetailsActivity";
    private static final String PREFS_NAME = "UserPrefs";
    private static final String BASE_URL = "http://192.168.153.1:8082/";

    // UI Components
    private TextInputEditText usernameBox, emailBox, phoneBox;
    private TextInputLayout usernameLayout, emailLayout, phoneLayout;
    private MaterialButton continueBtn;
    private TextView skipButton;
    private FrameLayout loadingOverlay;

    // Data & Services
    private ApiService apiService;
    private SharedPreferences prefs;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_details);

        setupStatusBar();
        initializeComponents();
        setupViews();
        setupClickListeners();
        prefillUserData();

        Log.d(TAG, "UserDetailsActivity created successfully");
    }

    private void setupStatusBar() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(android.R.color.transparent));
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }

    private void initializeComponents() {
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize SharedPreferences
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        currentUserId = firebaseUser != null ? firebaseUser.getUid() : null;

        Log.d(TAG, "Components initialized, User ID: " + currentUserId);
    }

    private void setupViews() {
        // Find views
        usernameBox = findViewById(R.id.usernameBox);
        emailBox = findViewById(R.id.emailBox);
        phoneBox = findViewById(R.id.phoneBox);

        usernameLayout = (TextInputLayout) usernameBox.getParent().getParent();
        emailLayout = (TextInputLayout) emailBox.getParent().getParent();
        phoneLayout = (TextInputLayout) phoneBox.getParent().getParent();

        continueBtn = findViewById(R.id.continueBtn);
        skipButton = findViewById(R.id.skipButton);
        loadingOverlay = findViewById(R.id.loadingOverlay);
    }

    private void setupClickListeners() {
        continueBtn.setOnClickListener(v -> {
            Log.d(TAG, "Continue button clicked");
            if (validateInputs()) {
                saveUserDetails();
            }
        });

        skipButton.setOnClickListener(v -> {
            Log.d(TAG, "Skip button clicked");
            showSkipConfirmation();
        });
    }

    private void prefillUserData() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            // Prefill phone number from Firebase
            if (firebaseUser.getPhoneNumber() != null) {
                phoneBox.setText(firebaseUser.getPhoneNumber());
                Log.d(TAG, "Phone number prefilled from Firebase");
            }

            // Prefill email if available
            if (firebaseUser.getEmail() != null) {
                emailBox.setText(firebaseUser.getEmail());
                Log.d(TAG, "Email prefilled from Firebase");
            }

            // Prefill display name if available
            if (firebaseUser.getDisplayName() != null) {
                usernameBox.setText(firebaseUser.getDisplayName());
                Log.d(TAG, "Display name prefilled from Firebase");
            }
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Clear previous errors
        usernameLayout.setError(null);
        emailLayout.setError(null);
        phoneLayout.setError(null);

        // Get input values
        String username = usernameBox.getText().toString().trim();
        String email = emailBox.getText().toString().trim();
        String phoneNumber = phoneBox.getText().toString().trim();

        // Validate username
        if (TextUtils.isEmpty(username)) {
            usernameLayout.setError("Full name is required");
            usernameBox.requestFocus();
            isValid = false;
        } else if (username.length() < 2) {
            usernameLayout.setError("Name must be at least 2 characters long");
            usernameBox.requestFocus();
            isValid = false;
        } else if (!isValidName(username)) {
            usernameLayout.setError("Please enter a valid name");
            usernameBox.requestFocus();
            isValid = false;
        }

        // Validate email
        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Email address is required");
            if (isValid) emailBox.requestFocus();
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Please enter a valid email address");
            if (isValid) emailBox.requestFocus();
            isValid = false;
        }

        // Validate phone number
        if (TextUtils.isEmpty(phoneNumber)) {
            phoneLayout.setError("Phone number is required");
            if (isValid) phoneBox.requestFocus();
            isValid = false;
        } else if (!isValidPhoneNumber(phoneNumber)) {
            phoneLayout.setError("Please enter a valid phone number");
            if (isValid) phoneBox.requestFocus();
            isValid = false;
        }

        Log.d(TAG, "Input validation result: " + isValid);
        return isValid;
    }

    private boolean isValidName(String name) {
        // Allow letters, spaces, and common name characters
        return name.matches("^[a-zA-Z\\s.''-]{2,50}$");
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        // Remove spaces and special characters for validation
        String cleanNumber = phoneNumber.replaceAll("[\\s\\-\\(\\)]", "");

        // Check if it's a valid phone number format
        if (cleanNumber.startsWith("+")) {
            return cleanNumber.length() >= 10 && cleanNumber.length() <= 15;
        } else if (cleanNumber.length() == 10) {
            return true; // Assume it's a local number
        }

        return false;
    }

    private void saveUserDetails() {
        if (currentUserId == null) {
            showError("User authentication failed. Please try again.");
            return;
        }

        showLoading(true);

        String username = usernameBox.getText().toString().trim();
        String email = emailBox.getText().toString().trim();
        String phoneNumber = phoneBox.getText().toString().trim();

        // Ensure phone number has country code
        if (!phoneNumber.startsWith("+")) {
            phoneNumber = "+91" + phoneNumber; // Default to India
        }

        User user = new User(currentUserId, username, email, phoneNumber);

        Log.d(TAG, "Attempting to save user: " + username + ", " + email);

        Call<User> call = apiService.postUser(user);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                showLoading(false);

                if (response.isSuccessful()) {
                    Log.d(TAG, "User details saved successfully");
                    handleSaveSuccess();
                } else {
                    Log.e(TAG, "Failed to save user details: " + response.code());
                    handleSaveError("Failed to save details. Please try again. (Code: " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Network error while saving user details", t);
                handleSaveError("Network error. Please check your connection and try again.");
            }
        });
    }

    private void handleSaveSuccess() {
        // Save completion status in SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isUserDetailsSaved_" + currentUserId, true);
        editor.putString("saved_username", usernameBox.getText().toString().trim());
        editor.putString("saved_email", emailBox.getText().toString().trim());
        editor.apply();

        Toast.makeText(this, "Profile setup completed successfully!", Toast.LENGTH_SHORT).show();

        // Navigate to MainActivity
        navigateToMainActivity();
    }

    private void handleSaveError(String errorMessage) {
        showError(errorMessage);

        // Re-enable the button and show error
        continueBtn.setEnabled(true);
        continueBtn.setText("Try Again");
    }

    private void showSkipConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Skip Profile Setup?")
                .setMessage("You can complete your profile later from the settings. Are you sure you want to skip?")
                .setPositiveButton("Skip", (dialog, which) -> {
                    Log.d(TAG, "User chose to skip profile setup");
                    navigateToMainActivity();
                })
                .setNegativeButton("Continue Setup", null)
                .show();
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        // Add transition animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }

        // Disable/enable user interactions
        continueBtn.setEnabled(!show);
        skipButton.setEnabled(!show);
        usernameBox.setEnabled(!show);
        emailBox.setEnabled(!show);
        phoneBox.setEnabled(!show);

        if (show) {
            continueBtn.setText("Saving...");
        } else {
            continueBtn.setText("Complete Setup");
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        // Show confirmation dialog before going back
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Exit Setup?")
                .setMessage("Your profile setup is not complete. Are you sure you want to exit?")
                .setPositiveButton("Exit", (dialog, which) -> {
                    super.onBackPressed();
                })
                .setNegativeButton("Continue", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Auto-focus username field when activity resumes
        if (usernameBox != null && usernameBox.getText().toString().trim().isEmpty()) {
            usernameBox.requestFocus();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "UserDetailsActivity destroyed");
    }
}