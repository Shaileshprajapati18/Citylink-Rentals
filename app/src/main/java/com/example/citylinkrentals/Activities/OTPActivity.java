package com.example.citylinkrentals.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.citylinkrentals.R;
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

    private OtpView otpView;
    private Button continueBtn;
    private TextView resendText;
    private TextView otpInstruction;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String verificationId;
    private String phoneNumber;
    private static final String TAG = "OTPActivity";
    private static final int MAX_RETRIES = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpactivity);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Set status bar color
        getWindow().setStatusBarColor(getResources().getColor(R.color.main_color));

        // Initialize UI elements
        otpView = findViewById(R.id.otp_view);
        continueBtn = findViewById(R.id.continueBtn);
        resendText = findViewById(R.id.resendText);
        otpInstruction = findViewById(R.id.otpInstruction);

        // Get data from intent
        verificationId = getIntent().getStringExtra("verificationId");
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        otpInstruction.setText("Please type the verification code sent to " + phoneNumber);

        // Continue button click listener
        continueBtn.setOnClickListener(v -> {
            String code = otpView.getText().toString().trim();
            if (code.isEmpty() || code.length() < 6) {
                otpView.setError("Enter valid OTP");
                otpView.requestFocus();
                return;
            }
            verifyCode(code);
        });

        // Resend OTP click listener
        resendText.setOnClickListener(v -> resendVerificationCode(phoneNumber, MAX_RETRIES));
    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void resendVerificationCode(String phoneNumber, int remainingAttempts) {
        if (remainingAttempts <= 0) {
            Toast.makeText(this, "Max retry attempts reached. Please try again later.", Toast.LENGTH_LONG).show();
            return;
        }

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Log.e(TAG, "Resend failed: " + e.getMessage());
                        if (e.getMessage().contains("BILLING_NOT_ENABLED")) {
                            Toast.makeText(OTPActivity.this,
                                    "Billing not enabled in Google Cloud Console. Please contact support.",
                                    Toast.LENGTH_LONG).show();
                        } else if (e.getMessage().contains("Integrity")) {
                            Toast.makeText(OTPActivity.this,
                                    "Please update Google Play Services or try again.",
                                    Toast.LENGTH_LONG).show();
                            resendVerificationCode(phoneNumber, remainingAttempts - 1);
                        } else {
                            Toast.makeText(OTPActivity.this,
                                    "Resend failed: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCodeSent(@NonNull String newVerificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        verificationId = newVerificationId;
                        Toast.makeText(OTPActivity.this, "OTP resent successfully", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(OTPActivity.this, "Verification Successful", Toast.LENGTH_SHORT).show();
                        checkUserDetails();
                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            FirebaseAuthUserCollisionException exception = (FirebaseAuthUserCollisionException) task.getException();
                            AuthCredential existingCredential = exception.getUpdatedCredential();
                            mAuth.getCurrentUser().linkWithCredential(existingCredential)
                                    .addOnSuccessListener(authResult -> checkUserDetails())
                                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to link accounts: " + e.getMessage(), Toast.LENGTH_LONG).show());
                        } else {
                            Toast.makeText(OTPActivity.this, "Verification Failed: Invalid OTP", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void checkUserDetails() {
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // User details exist, go to MainActivity
                    startActivity(new Intent(OTPActivity.this, MainActivity.class));
                    finish();
                } else {
                    // User details don't exist, go to UserDetailsActivity
                    startActivity(new Intent(OTPActivity.this, UserDetailsActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
                Toast.makeText(OTPActivity.this, "Error checking user details: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}