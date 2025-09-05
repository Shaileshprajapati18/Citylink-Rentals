package com.example.citylinkrentals.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.citylinkrentals.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText phoneBox;
    private MaterialButton continueBtn, btnGoogleSignIn;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private String verificationId;
    private static final int RC_SIGN_IN = 9001;
    private static final int MAX_RETRIES = 3;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        phoneBox = findViewById(R.id.phoneEditText);
        continueBtn = findViewById(R.id.continueBtn);
        btnGoogleSignIn = findViewById(R.id.googleSignInBtn);

        getWindow().setStatusBarColor(getResources().getColor(R.color.main_color));

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());

        continueBtn.setOnClickListener(v -> {
            String phoneNumber = phoneBox.getText().toString().trim();
            if (phoneNumber.isEmpty() || phoneNumber.length() < 10) {
                phoneBox.setError("Enter a valid phone number");
                phoneBox.requestFocus();
                return;
            }

            if (!phoneNumber.startsWith("+")) {
                phoneNumber = "+91" + phoneNumber;
            }

            sendVerificationCode(phoneNumber, MAX_RETRIES);
        });
    }

    private void signInWithGoogle() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                } else {
                    Toast.makeText(this, "Google Sign-In failed: No account selected", Toast.LENGTH_SHORT).show();
                }
            } catch (ApiException e) {
                Log.e(TAG, "Google Sign-In failed: " + e.getStatusCode());
                Toast.makeText(this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Google Sign-In Successful", Toast.LENGTH_SHORT).show();
                        navigateToUserDetails();
                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            FirebaseAuthUserCollisionException exception = (FirebaseAuthUserCollisionException) task.getException();
                            AuthCredential existingCredential = exception.getUpdatedCredential();
                            mAuth.getCurrentUser().linkWithCredential(existingCredential)
                                    .addOnSuccessListener(authResult -> navigateToUserDetails())
                                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to link accounts: " + e.getMessage(), Toast.LENGTH_LONG).show());
                        } else {
                            Log.e(TAG, "Firebase Google Auth failed: " + task.getException());
                            Toast.makeText(LoginActivity.this, "Google Sign-In Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void navigateToUserDetails() {
        Intent intent = new Intent(LoginActivity.this, UserDetailsActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendVerificationCode(String phoneNumber, int remainingAttempts) {
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
                        Log.e(TAG, "Verification failed: " + e.getMessage());
                        if (e.getMessage().contains("BILLING_NOT_ENABLED")) {
                            Toast.makeText(LoginActivity.this,
                                    "Billing not enabled in Google Cloud Console. Please contact support.",
                                    Toast.LENGTH_LONG).show();
                        } else if (e.getMessage().contains("Integrity")) {
                            Toast.makeText(LoginActivity.this,
                                    "Please update Google Play Services or try again.",
                                    Toast.LENGTH_LONG).show();
                            sendVerificationCode(phoneNumber, remainingAttempts - 1);
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "Verification failed: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        LoginActivity.this.verificationId = verificationId;
                        Intent intent = new Intent(LoginActivity.this, OTPActivity.class);
                        intent.putExtra("phoneNumber", phoneNumber);
                        intent.putExtra("verificationId", verificationId);
                        startActivity(intent);
                    }
                });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Phone Authentication Successful", Toast.LENGTH_SHORT).show();
                        navigateToUserDetails();
                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            FirebaseAuthUserCollisionException exception = (FirebaseAuthUserCollisionException) task.getException();
                            AuthCredential existingCredential = exception.getUpdatedCredential();
                            mAuth.getCurrentUser().linkWithCredential(existingCredential)
                                    .addOnSuccessListener(authResult -> navigateToUserDetails())
                                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to link accounts: " + e.getMessage(), Toast.LENGTH_LONG).show());
                        } else {
                            Toast.makeText(LoginActivity.this, "Phone Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}