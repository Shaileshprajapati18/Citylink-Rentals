package com.example.citylinkrentals.Fragments;

import static android.app.ProgressDialog.show;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.citylinkrentals.Activities.EditProfileActivity;
import com.example.citylinkrentals.Activities.FavoritePropertiesActivity;
import com.example.citylinkrentals.Activities.HelpAndSupportActivity;
import com.example.citylinkrentals.Activities.LoginActivity;
import com.example.citylinkrentals.Activities.PrivacyPolicyActivity;
import com.example.citylinkrentals.Activities.UserDetailsActivity;
import com.example.citylinkrentals.R;
import com.example.citylinkrentals.model.User;
import com.example.citylinkrentals.model.UserProfileResponse;
import com.example.citylinkrentals.network.ApiService;
import com.example.citylinkrentals.network.RetrofitClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.suke.widget.SwitchButton;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.concurrent.TimeUnit;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int RC_GOOGLE_SIGN_IN = 9001;

    private LinearLayout deleteAccountAction;
    private FirebaseAuth mAuth;
    private CircleImageView ivProfilePicture;
    private TextView tvProfileName, tvUserPhone, tvUserEmail, tvAppVersion;
    private Chip chipVerified;
    private MaterialButton btnLogout;
    private ProgressBar progressBar;
    private Button btnRetryApiCall;
    ImageView btnEditProfile;
    LinearLayout helpAction, privacyAction, favoriteAction;
    private FirebaseUser currentUser;
    private ApiService apiService;
    private SwitchButton switchNotifications;

    // Google Sign-In
    private GoogleSignInClient mGoogleSignInClient;

    // OTP variables
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private CountDownTimer mCountDownTimer;
    private AlertDialog deleteAccountDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initializeViews(view);
        initializeData();
        setupClickListeners();
        loadUserProfile();

        return view;
    }

    private void initializeViews(View view) {
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvUserPhone = view.findViewById(R.id.tvUserPhone);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvAppVersion = view.findViewById(R.id.tvAppVersion);
        chipVerified = view.findViewById(R.id.chipVerified);
        btnLogout = view.findViewById(R.id.btnLogout);
        progressBar = view.findViewById(R.id.progressBar);
        btnRetryApiCall = view.findViewById(R.id.btnRetryApiCall);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        helpAction = view.findViewById(R.id.helpAction);
        privacyAction = view.findViewById(R.id.privacyAction);
        favoriteAction = view.findViewById(R.id.favoriteAction);
        switchNotifications = view.findViewById(R.id.switch_button);
        mAuth = FirebaseAuth.getInstance();

        // Initialize Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        deleteAccountAction = view.findViewById(R.id.deleteAccountAction);
        deleteAccountAction.setOnClickListener(v -> showDeleteAccountDialog());

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EditProfileActivity.class);
            startActivity(intent);
        });

        ValueAnimator colorAnimation = ValueAnimator.ofObject(
                new ArgbEvaluator(),
                Color.parseColor("#4CAF50"),
                Color.parseColor("#5DDE60")
        );

        colorAnimation.setDuration(2000);
        colorAnimation.setRepeatCount(ValueAnimator.INFINITE);
        colorAnimation.setRepeatMode(ValueAnimator.REVERSE);

        colorAnimation.addUpdateListener(animator -> {
            int color = (int) animator.getAnimatedValue();
            chipVerified.setChipBackgroundColor(ColorStateList.valueOf(color));
        });

        colorAnimation.start();

        chipVerified.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Verified Account", Toast.LENGTH_SHORT).show();
        });

        updateNotificationSwitchState();
    }

    private void showDeleteAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_delete_account, null);

        // Find views
        LinearLayout googleReauthSection = dialogView.findViewById(R.id.google_reauth_section);
        LinearLayout otpReauthSection = dialogView.findViewById(R.id.otp_reauth_section);
        MaterialButton btnGoogleSignin = dialogView.findViewById(R.id.btn_google_signin);
        TextView tvPhoneNumber = dialogView.findViewById(R.id.tv_phone_number);
        TextInputEditText etOtp = dialogView.findViewById(R.id.et_otp);
        MaterialButton btnResendOtp = dialogView.findViewById(R.id.btn_resend_otp);
        TextView tvOtpTimer = dialogView.findViewById(R.id.tv_otp_timer);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnDelete = dialogView.findViewById(R.id.btn_delete);

        builder.setView(dialogView);
        deleteAccountDialog = builder.create();

        // Check user's sign-in method and show appropriate re-authentication
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            for (com.google.firebase.auth.UserInfo profile : user.getProviderData()) {
                String providerId = profile.getProviderId();

                if (providerId.equals("google.com")) {

                    googleReauthSection.setVisibility(View.VISIBLE);
                    otpReauthSection.setVisibility(View.GONE);
                } else if (providerId.equals("phone")) {

                    googleReauthSection.setVisibility(View.GONE);
                    otpReauthSection.setVisibility(View.VISIBLE);

                    String phoneNumber = user.getPhoneNumber();
                    if (phoneNumber != null && phoneNumber.length() > 10) {
                        String maskedNumber = phoneNumber.substring(0, phoneNumber.length() - 5)
                                + "XXXXX";
                        tvPhoneNumber.setText(maskedNumber);
                    }

                    sendOtpForReauth(user.getPhoneNumber());
                }
            }
        }

        // Set up click listeners
        btnGoogleSignin.setOnClickListener(v -> {
            // Start Google Sign In
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
        });

        btnResendOtp.setOnClickListener(v -> {
            if (user != null && user.getPhoneNumber() != null) {
                resendOtpForReauth(user.getPhoneNumber());
            }
        });

        btnCancel.setOnClickListener(v -> {
            if (mCountDownTimer != null) {
                mCountDownTimer.cancel();
            }
            deleteAccountDialog.dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            if (googleReauthSection.getVisibility() == View.VISIBLE) {
                // For Google, we need to wait for onActivityResult
                Toast.makeText(getContext(), "Please complete Google re-authentication first", Toast.LENGTH_SHORT).show();
            } else if (otpReauthSection.getVisibility() == View.VISIBLE) {
                // For OTP, verify the entered code
                String otp = etOtp.getText().toString().trim();
                if (otp.isEmpty() || otp.length() != 6) {
                    etOtp.setError("Please enter a valid 6-digit OTP");
                    return;
                }

                // Disable delete button while processing
                btnDelete.setEnabled(false);
                btnDelete.setText("Verifying...");

                // Verify OTP and proceed with deletion
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
                reauthenticateAndDelete(credential);
            }
        });

        deleteAccountDialog.show();
    }

    private void sendOtpForReauth(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                requireActivity(),
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        // Auto-retrieval or instant verification
                        reauthenticateAndDelete(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull com.google.firebase.FirebaseException e) {
                        Toast.makeText(requireContext(),
                                "Verification failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        mVerificationId = verificationId;
                        mResendToken = token;

                        // Start countdown timer for resend button
                        startResendTimer();
                    }
                });
    }

    private void resendOtpForReauth(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                requireActivity(),
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        reauthenticateAndDelete(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull com.google.firebase.FirebaseException e) {
                        Toast.makeText(requireContext(),
                                "Verification failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        mVerificationId = verificationId;
                        mResendToken = token;

                        // Start countdown timer for resend button
                        startResendTimer();

                        Toast.makeText(requireContext(), "OTP resent successfully",
                                Toast.LENGTH_SHORT).show();
                    }
                },
                mResendToken);
    }

    private void startResendTimer() {
        if (deleteAccountDialog == null) return;

        TextView tvOtpTimer = deleteAccountDialog.findViewById(R.id.tv_otp_timer);
        MaterialButton btnResendOtp = deleteAccountDialog.findViewById(R.id.btn_resend_otp);

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }

        btnResendOtp.setEnabled(false);
        tvOtpTimer.setVisibility(View.VISIBLE);

        mCountDownTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvOtpTimer.setText("Resend OTP in " + millisUntilFinished / 1000 + "s");
            }

            @Override
            public void onFinish() {
                btnResendOtp.setEnabled(true);
                tvOtpTimer.setVisibility(View.GONE);
            }
        }.start();
    }

    private void reauthenticateAndDelete(AuthCredential credential) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Re-authentication successful, proceed with deletion
                        deleteUserData(user.getUid());
                    } else {
                        if (deleteAccountDialog != null && deleteAccountDialog.isShowing()) {
                            MaterialButton btnDelete = deleteAccountDialog.findViewById(R.id.btn_delete);
                            btnDelete.setEnabled(true);
                            btnDelete.setText("Delete");
                        }
                        Toast.makeText(requireContext(),
                                "Re-authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void deleteUserData(String userId) {
        // Delete user data from Firestore
        progressBar.setVisibility(View.VISIBLE);

        // Note: Replace "users" with your actual Firestore collection name
        // This is a placeholder for your actual deletion logic
        /*
        db.collection("users").document(userId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                // Delete user authentication
                deleteUserAuth();
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(),
                    "Failed to delete user data: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            });
        */

        // For now, directly proceed to delete authentication
        // In a real app, you would delete user data from Firestore first
        deleteUserAuth();
    }

    private void deleteUserAuth() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            user.delete()
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);

                        if (deleteAccountDialog != null && deleteAccountDialog.isShowing()) {
                            deleteAccountDialog.dismiss();
                        }

                        if (task.isSuccessful()) {
                            // Sign out and navigate to login screen
                            mAuth.signOut();
                            mGoogleSignInClient.signOut();

                            Toast.makeText(requireContext(),
                                    "Account deleted successfully",
                                    Toast.LENGTH_SHORT).show();

                            // Navigate to login activity
                            Intent intent = new Intent(requireContext(), LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            requireActivity().finish();
                        } else {
                            Toast.makeText(requireContext(),
                                    "Failed to delete account: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void initializeData() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        apiService = RetrofitClient.getApiService();

        try {
            String versionName = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionName;
            tvAppVersion.setText("Version " + versionName);
        } catch (Exception e) {
            tvAppVersion.setText("Version 1.0.0");
        }
    }

    private void setupClickListeners() {
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
        btnRetryApiCall.setOnClickListener(v -> {
            btnRetryApiCall.setVisibility(View.GONE);
            loadUserDataFromAPI();
        });

        helpAction.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), HelpAndSupportActivity.class));
        });

        privacyAction.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), PrivacyPolicyActivity.class));
        });

        favoriteAction.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), FavoritePropertiesActivity.class));
        });

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkAndRequestNotificationPermission();
            } else {
                showDisableNotificationDialog();
            }
        });
    }

    private void loadUserProfile() {
        if (currentUser == null) {
            showError("User not authenticated");
            return;
        }

        loadProfileImageFromFirebase();
        loadFirebaseUserData();
        loadUserDataFromAPI();
    }

    private void loadProfileImageFromFirebase() {
        if (currentUser != null && currentUser.getPhotoUrl() != null) {
            Glide.with(requireContext())
                    .load(currentUser.getPhotoUrl())
                    .placeholder(R.drawable.profile_image_placeholder)
                    .error(R.drawable.profile_image_placeholder)
                    .into(ivProfilePicture);
        } else {
            ivProfilePicture.setImageResource(R.drawable.profile_image_placeholder);
        }
    }

    private void loadFirebaseUserData() {
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.trim().isEmpty()) {
                tvProfileName.setText(displayName);
            } else {
                tvProfileName.setText("User");
            }

            String email = currentUser.getEmail();
            if (email != null && !email.trim().isEmpty()) {
                tvUserEmail.setText(email);
            } else {
                tvUserEmail.setText("Email not available");
            }

            String phoneNumber = currentUser.getPhoneNumber();
            if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                tvUserPhone.setText(phoneNumber);
            } else {
                tvUserPhone.setText("Phone not available");
            }

            boolean isVerified = currentUser.isEmailVerified();
            chipVerified.setVisibility(isVerified ? View.VISIBLE : View.GONE);
        }
    }

    private void loadUserDataFromAPI() {
        if (currentUser == null) {
            Log.e(TAG, "Current user is null, cannot load API data");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        String firebaseUid = currentUser.getUid();
        Log.d(TAG, "Loading user data from API for UID: " + firebaseUid);

        Call<UserProfileResponse> call = apiService.getUserProfile(firebaseUid);
        call.enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                progressBar.setVisibility(View.GONE);

                Log.d(TAG, "API Response code: " + response.code());
                Log.d(TAG, "API Response message: " + response.message());
                Log.d(TAG, "API Response is successful: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    UserProfileResponse userProfileResponse = response.body();
                    Log.d(TAG, "Received response: " + userProfileResponse.toString());

                    if (userProfileResponse.getMessageBody() != null && !userProfileResponse.getMessageBody().isEmpty()) {
                        User userProfile = userProfileResponse.getMessageBody().get(0);
                        Log.d(TAG, "Extracted user data: " + userProfile.toString());
                        updateUIWithUserProfile(userProfile);
                    } else {
                        Log.e(TAG, "API response has empty messageBody");
                        showError("No user data found in response");
                    }
                } else {
                    handleApiError(response.code());
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "API call failed: " + t.getMessage());
                Log.e(TAG, "API call failed, stack trace: ", t);
                showError("Network error: " + t.getMessage());
                btnRetryApiCall.setVisibility(View.VISIBLE);
            }
        });
    }

    private void handleApiError(int statusCode) {
        String errorMessage;

        switch (statusCode) {
            case 404:
                errorMessage = "User profile not found in our system";
                showUserNotFoundMessage();
                break;
            case 400:
                errorMessage = "Invalid request parameters";
                break;
            case 401:
                errorMessage = "Authentication failed";
                break;
            case 500:
                errorMessage = "Server error occurred";
                break;
            default:
                errorMessage = "Failed to load user data (Error: " + statusCode + ")";
                break;
        }

        showError(errorMessage);
        Log.e(TAG, "API Error: " + statusCode + " - " + errorMessage);
    }

    private void showUserNotFoundMessage() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Profile Not Found")
                .setMessage("Your account exists but we couldn't find your profile in our system. Would you like to create a profile now?")
                .setPositiveButton("Create Profile", (dialog, which) -> {
                    Intent intent = new Intent(getContext(), UserDetailsActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("Maybe Later", null)
                .setCancelable(false)
                .show();
    }

    private void updateUIWithUserProfile(User userProfile) {
        if (userProfile == null) {
            Log.e(TAG, "updateUIWithUserProfile called with null user");
            showError("Received empty user data");
            return;
        }

        Log.d(TAG, "Updating UI with user data:");
        Log.d(TAG, "Username: " + userProfile.getUsername());
        Log.d(TAG, "Email: " + userProfile.getEmail());
        Log.d(TAG, "Phone: " + userProfile.getPhoneNumber());
        Log.d(TAG, "Firebase UID: " + userProfile.getFirebaseUid());

        if (userProfile.getUsername() != null && !userProfile.getUsername().trim().isEmpty()) {
            Log.d(TAG, "Setting username to: " + userProfile.getUsername());
            tvProfileName.setText(userProfile.getUsername());
        } else {
            Log.d(TAG, "Username is null or empty, keeping current value");
        }

        if (userProfile.getPhoneNumber() != null && !userProfile.getPhoneNumber().trim().isEmpty()) {
            Log.d(TAG, "Setting phone to: " + userProfile.getPhoneNumber());
            tvUserPhone.setText(userProfile.getPhoneNumber());
        } else {
            Log.d(TAG, "Phone is null or empty, keeping current value");
        }

        if (userProfile.getEmail() != null && !userProfile.getEmail().trim().isEmpty()) {
            Log.d(TAG, "Setting email to: " + userProfile.getEmail());
            tvUserEmail.setText(userProfile.getEmail());
        } else {
            Log.d(TAG, "Email is null or empty, keeping current value");
        }
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        FirebaseAuth.getInstance().signOut();
        SharedPreferences prefs = requireContext().getSharedPreferences("app_settings", requireContext().MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Notification permission methods
    private void updateNotificationSwitchState() {
        boolean hasPermission = hasNotificationPermission();
        switchNotifications.setChecked(hasPermission);
    }

    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        // For devices below Android 13, notifications are enabled by default
        return true;
    }

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission()) {
                // Request permission
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            } else {
                // Permission already granted
                Toast.makeText(requireContext(), "Notifications are already enabled", Toast.LENGTH_SHORT).show();
            }
        } else {
            // For devices below Android 13, no need to request permission
            Toast.makeText(requireContext(), "Notifications enabled", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDisableNotificationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Disable Notifications")
                .setMessage("Are you sure you want to disable notifications? You can manage notification permissions in app settings.")
                .setPositiveButton("Disable", (dialog, which) -> {

                    Toast.makeText(requireContext(), "Notifications disabled. You can enable them again in app settings.", Toast.LENGTH_LONG).show();

                    openAppSettings();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {

                    switchNotifications.setChecked(true);
                })
                .setCancelable(false)
                .show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(requireContext(), "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(requireContext(), "Notification permission denied", Toast.LENGTH_SHORT).show();
                switchNotifications.setChecked(false);

                showPermissionDeniedDialog();
            }
        }
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Permission Required")
                .setMessage("To receive notifications, you need to grant the permission. Please go to app settings and enable notifications.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    openAppSettings();
                })
                .setNegativeButton("Cancel", null)
                .setCancelable(false)
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    // Get Google credentials
                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

                    // Re-authenticate and delete
                    reauthenticateAndDelete(credential);
                }
            } catch (ApiException e) {
                Toast.makeText(getContext(), "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
        updateNotificationSwitchState();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }
}