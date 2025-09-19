package com.example.citylinkrentals.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.citylinkrentals.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mukeshsolanki.OtpView;

import java.util.concurrent.TimeUnit;

public class OTPActivity extends AppCompatActivity {

    private static final String TAG = "OTPActivity";
    private static final int MAX_RETRIES = 3;
    private static final long COUNTDOWN_DURATION = 60000; // 60 seconds

    // UI Components
    private OtpView otpView;
    private MaterialButton continueBtn;
    private TextView resendText, otpInstruction, timerText, tvContactSupport;
    private ImageView backButton;
    private FrameLayout loadingOverlay;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Data
    private String verificationId;
    private String phoneNumber;
    private CountDownTimer countDownTimer;
    private boolean canResend = false;
    private int retryCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpactivity);

        setupStatusBar();
        initializeFirebase();
        initializeViews();
        getIntentData();
        setupClickListeners();
        startCountdownTimer();

        Log.d(TAG, "OTPActivity created successfully");
    }

    private void setupStatusBar() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(android.R.color.transparent));
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    private void initializeViews() {
        otpView = findViewById(R.id.otp_view);
        continueBtn = findViewById(R.id.continueBtn);
        resendText = findViewById(R.id.resendText);
        otpInstruction = findViewById(R.id.otpInstruction);
        timerText = findViewById(R.id.timerText);
        backButton = findViewById(R.id.backButton);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        tvContactSupport = findViewById(R.id.tvContactSupport);
    }

    private void getIntentData() {
        verificationId = getIntent().getStringExtra("verificationId");
        phoneNumber = getIntent().getStringExtra("phoneNumber");

        if (phoneNumber != null) {
            String maskedNumber = maskPhoneNumber(phoneNumber);
            otpInstruction.setText("We've sent a 6-digit code to " + maskedNumber);
        }
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return phoneNumber;
        }

        // Show first 3 digits and last 2 digits, mask the middle
        String prefix = phoneNumber.substring(0, 3);
        String suffix = phoneNumber.substring(phoneNumber.length() - 2);
        String masked = prefix + "****" + suffix;
        return masked;
    }

    private void setupClickListeners() {
        // Back button
        backButton.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            onBackPressed();
        });

        // Continue button
        continueBtn.setOnClickListener(v -> {
            String code = otpView.getText().toString().trim();
            Log.d(TAG, "Continue button clicked, OTP length: " + code.length());

            if (validateOTP(code)) {
                showLoading(true);
                verifyCode(code);
            }
        });

        // Resend OTP
        resendText.setOnClickListener(v -> {
            Log.d(TAG, "Resend OTP clicked, canResend: " + canResend);
            if (canResend && retryCount < MAX_RETRIES) {
                resendVerificationCode();
            } else if (retryCount >= MAX_RETRIES) {
                showMaxRetryMessage();
            } else {
                Toast.makeText(this, "Please wait before requesting another code", Toast.LENGTH_SHORT).show();
            }
        });

        // Contact Support
        tvContactSupport.setOnClickListener(v -> {
            Log.d(TAG, "Contact support clicked");
            openContactSupport();
        });

        // OTP completion listener
        otpView.setOtpCompletionListener(otp -> {
            Log.d(TAG, "OTP completed: " + otp.length());
            if (otp.length() == 6) {
                // Auto-verify when 6 digits are entered
                showLoading(true);
                verifyCode(otp);
            }
        });
    }

    private boolean validateOTP(String code) {
        if (code.isEmpty()) {
            showError("Please enter the verification code");
            return false;
        }

        if (code.length() != 6) {
            showError("Please enter a valid 6-digit code");
            return false;
        }

        return true;
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        otpView.requestFocus();
    }

    private void verifyCode(String code) {
        Log.d(TAG, "Verifying code...");

        if (verificationId == null) {
            showLoading(false);
            showError("Verification ID is missing. Please try again.");
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        Log.d(TAG, "Phone auth successful");
                        Toast.makeText(this, "Verification successful!", Toast.LENGTH_SHORT).show();
                        checkUserDetails();

                    } else {
                        Log.e(TAG, "Phone auth failed", task.getException());
                        handleAuthFailure(task.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Sign in failed", e);
                    showError("Verification failed. Please check the code and try again.");
                });
    }

    private void handleAuthFailure(Exception exception) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            FirebaseAuthUserCollisionException collisionException = (FirebaseAuthUserCollisionException) exception;
            AuthCredential existingCredential = collisionException.getUpdatedCredential();

            if (existingCredential != null && mAuth.getCurrentUser() != null) {
                mAuth.getCurrentUser().linkWithCredential(existingCredential)
                        .addOnSuccessListener(authResult -> {
                            Log.d(TAG, "Accounts linked successfully");
                            checkUserDetails();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to link accounts", e);
                            showError("Failed to link accounts. Please try again.");
                        });
            } else {
                showError("Account linking failed. Please try again.");
            }
        } else {
            showError("Invalid verification code. Please try again.");
        }
    }

    private void resendVerificationCode() {
        if (phoneNumber == null) {
            showError("Phone number is missing");
            return;
        }

        retryCount++;
        Log.d(TAG, "Resending verification code, attempt: " + retryCount);

        showLoading(true);

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        Log.d(TAG, "Verification completed automatically");
                        showLoading(false);
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        showLoading(false);
                        Log.e(TAG, "Resend verification failed", e);
                        handleResendFailure(e);
                    }

                    @Override
                    public void onCodeSent(@NonNull String newVerificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        showLoading(false);
                        verificationId = newVerificationId;

                        Toast.makeText(OTPActivity.this, "New code sent successfully!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "New verification code sent");

                        // Clear current OTP and restart timer
                        otpView.setText("");
                        startCountdownTimer();
                    }
                });
    }

    private void handleResendFailure(FirebaseException e) {
        String errorMessage = "Failed to resend code. Please try again.";

        if (e.getMessage() != null) {
            if (e.getMessage().contains("BILLING_NOT_ENABLED")) {
                errorMessage = "Service temporarily unavailable. Please contact support.";
            } else if (e.getMessage().contains("Integrity")) {
                errorMessage = "Please update Google Play Services and try again.";
            } else if (e.getMessage().contains("quota")) {
                errorMessage = "Too many attempts. Please try again later.";
            }
        }

        showError(errorMessage);
    }

    private void startCountdownTimer() {
        canResend = false;

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(COUNTDOWN_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                timerText.setText(String.format("Resend code in 00:%02d", secondsRemaining));
                timerText.setVisibility(View.VISIBLE);
                resendText.setAlpha(0.5f);
            }

            @Override
            public void onFinish() {
                canResend = true;
                timerText.setVisibility(View.GONE);
                resendText.setAlpha(1.0f);
                Log.d(TAG, "Countdown finished, resend enabled");
            }
        }.start();
    }

    private void showMaxRetryMessage() {
        Toast.makeText(this,
                "Maximum retry attempts reached. Please try again later or contact support.",
                Toast.LENGTH_LONG).show();
    }

    private void checkUserDetails() {
        if (mAuth.getCurrentUser() == null) {
            showError("Authentication failed. Please try again.");
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        Log.d(TAG, "Checking user details for: " + userId);

        showLoading(true);

        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                showLoading(false);

                if (snapshot.exists()) {
                    Log.d(TAG, "User details exist, navigating to MainActivity");
                    navigateToMainActivity();
                } else {
                    Log.d(TAG, "User details don't exist, navigating to UserDetailsActivity");
                    navigateToUserDetailsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Log.e(TAG, "Database error: " + error.getMessage());
                showError("Failed to verify user details. Please try again.");
            }
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToUserDetailsActivity() {
        Intent intent = new Intent(this, UserDetailsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }

        // Disable/enable user interactions
        continueBtn.setEnabled(!show);
        resendText.setEnabled(!show);
        otpView.setEnabled(!show);
    }

    private void openContactSupport() {
        // You can implement this to open support chat, email, or phone
        Toast.makeText(this, "Contact support feature coming soon", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        // Cancel timer and go back
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Clean up timer
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        Log.d(TAG, "OTPActivity destroyed");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Auto-focus OTP input when activity resumes
        if (otpView != null) {
            otpView.requestFocus();
        }
    }

    // Helper method for testing
    public void setVerificationIdForTesting(String verificationId) {
        this.verificationId = verificationId;
    }
}