package com.example.citylinkrentals.Activities;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.citylinkrentals.R;
import com.example.citylinkrentals.databinding.ActivitySplashBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private FirebaseAuth auth;
    private final long splashDuration = 2500L;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        setupStatusScreen();
        startAnimations();

        checkAuthenticationAndNavigate();

        try {
            String versionName = this.getPackageManager()
                    .getPackageInfo(this.getPackageName(), 0).versionName;
            binding.versionText.setText("Version " + versionName);
        } catch (Exception e) {
            binding.versionText.setText("Version 1.0.0");
        }
    }

    private void setupStatusScreen() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(android.R.color.transparent));
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }

    private void startAnimations() {

        binding.logoContainer.animate()
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(200)
                .start();

        binding.houseIcon.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .setInterpolator(new OvershootInterpolator(2f))
                .setStartDelay(300)
                .start();

        binding.tagline.setVisibility(View.VISIBLE);
        binding.tagline.animate()
                .alpha(0.9f)
                .setDuration(400)
                .setStartDelay(1000)
                .start();

        binding.progressIndicator.animate()
                .alpha(1f)
                .setDuration(400)
                .setStartDelay(1200)
                .start();

        binding.loadingText.animate()
                .alpha(1f)
                .setDuration(400)
                .setStartDelay(1300)
                .start();

        // Pulse animation for house icon
        startPulseAnimation();
    }

    private void startPulseAnimation() {
        ObjectAnimator scaleXAnimator =
                ObjectAnimator.ofFloat(binding.houseIcon, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleYAnimator =
                ObjectAnimator.ofFloat(binding.houseIcon, "scaleY", 1f, 1.1f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleXAnimator, scaleYAnimator);
        animatorSet.setDuration(1000);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.setStartDelay(1500);

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isFinishing()) {
                    animatorSet.start(); // Repeat the pulse
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        animatorSet.start();
    }

    private void checkAuthenticationAndNavigate() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser currentUser = auth.getCurrentUser();
            navigateToNextScreen(currentUser);
        }, splashDuration);
    }

    private void navigateToNextScreen(FirebaseUser currentUser) {
        Intent intent;
        if (currentUser == null) {
            // User not logged in
            intent = new Intent(this, LoginActivity.class);
        } else if (!isUserDetailsSaved(currentUser.getUid())) {
            // User logged in but details not saved
            intent = new Intent(this, UserDetailsActivity.class);
        } else {
            // User logged in and details saved
            intent = new Intent(this, MainActivity.class);
        }

        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private boolean isUserDetailsSaved(String userId) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getBoolean("isUserDetailsSaved_" + userId, false);
    }
}
