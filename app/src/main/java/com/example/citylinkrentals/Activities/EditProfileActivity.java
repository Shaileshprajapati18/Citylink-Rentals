package com.example.citylinkrentals.Activities;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.citylinkrentals.R;
import com.example.citylinkrentals.model.User;
import com.example.citylinkrentals.model.UserProfileResponse;
import com.example.citylinkrentals.network.ApiService;
import com.example.citylinkrentals.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    private TextInputEditText usernameBox, emailBox, phoneBox;
    private TextInputLayout usernameLayout, emailLayout, phoneLayout;
    private MaterialButton continueBtn;
    private View loadingOverlay;
    ImageView back_arrow;
    private FirebaseUser currentUser;
    private ApiService apiService;
    private User currentUserData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initializeViews();
        initializeData();
        loadUserProfile();

        back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.main_color, getTheme()));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decorView = window.getDecorView();
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                // decorView.setSystemUiVisibility(0);
            }
        }
    }

    private void initializeViews() {

        usernameBox = findViewById(R.id.usernameBox);
        emailBox = findViewById(R.id.emailBox);
        phoneBox = findViewById(R.id.phoneBox);
        back_arrow = findViewById(R.id.back_arrow);

        usernameLayout = (TextInputLayout) usernameBox.getParent().getParent();
        emailLayout = (TextInputLayout) emailBox.getParent().getParent();
        phoneLayout = (TextInputLayout) phoneBox.getParent().getParent();

        continueBtn = findViewById(R.id.continueBtn);
        continueBtn.setOnClickListener(v -> saveUserProfile());

        loadingOverlay = findViewById(R.id.loadingOverlay);
    }

    private void initializeData() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        apiService = RetrofitClient.getApiService();
    }

    private void loadUserProfile() {
        if (currentUser == null) {
            showError("User not authenticated");
            finish();
            return;
        }

        showLoading(true);

        Call<UserProfileResponse> call = apiService.getUserProfile(currentUser.getUid());
        call.enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    UserProfileResponse userProfileResponse = response.body();
                    if (userProfileResponse.getMessageBody() != null && !userProfileResponse.getMessageBody().isEmpty()) {
                        currentUserData = userProfileResponse.getMessageBody().get(0);
                        populateFields();
                    } else {
                        showError("No user data found");
                        finish();
                    }
                } else {
                    showError("Failed to load user data");
                    finish();
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                showLoading(false);
                showError("Network error: " + t.getMessage());
                finish();
            }
        });
    }

    private void populateFields() {
        if (currentUserData != null) {
            usernameBox.setText(currentUserData.getUsername());
            emailBox.setText(currentUserData.getEmail());
            phoneBox.setText(currentUserData.getPhoneNumber());
        }
    }

    private void saveUserProfile() {
        String username = usernameBox.getText().toString().trim();
        String email = emailBox.getText().toString().trim();
        String phone = phoneBox.getText().toString().trim();

        // Clear previous errors
        usernameLayout.setError(null);
        emailLayout.setError(null);
        phoneLayout.setError(null);

        // Validate input
        boolean isValid = true;

        if (username.isEmpty()) {
            usernameLayout.setError("Full name is required");
            isValid = false;
        }

        if (email.isEmpty()) {
            emailLayout.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Please enter a valid email address");
            isValid = false;
        }

        if (phone.isEmpty()) {
            phoneLayout.setError("Phone number is required");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Create a UserDTO object for the API request
        User userDTO = new User();
        userDTO.setFirebaseUid(currentUser.getUid());
        userDTO.setUsername(username);
        userDTO.setEmail(email);
        userDTO.setPhoneNumber(phone);

        showLoading(true);

        // Send update request to API
        Call<UserProfileResponse> call = apiService.updateUser(userDTO);
        call.enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    UserProfileResponse userProfileResponse = response.body();
                    if (userProfileResponse.getStatusCode() == 0) {
                        // Success
                        showSuccessDialog();
                    } else {
                        // Error from server
                        showError(userProfileResponse.getStatusMessage());
                    }
                } else {
                    showError("Failed to update profile");
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                showLoading(false);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void showLoading(boolean isLoading) {
        loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Profile Updated")
                .setMessage("Your profile has been updated successfully.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Set result and finish
                    setResult(RESULT_OK);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}