package com.example.citylinkrentals.Activities;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.citylinkrentals.Fragments.AiChatFragment;
import com.example.citylinkrentals.Fragments.HomeFragment;
import com.example.citylinkrentals.Fragments.ProfileFragment;
import com.example.citylinkrentals.Fragments.MyListingsFragment;
import com.example.citylinkrentals.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private Window window;
    private BottomNavigationView bottomNavigationView;
    private HomeFragment homeFragment = new HomeFragment();
    private MyListingsFragment myListingsFragment = new MyListingsFragment();
    private ProfileFragment profileFragment = new ProfileFragment();
    private AiChatFragment aiChatFragment = new AiChatFragment();
    private Fragment currentFragment;
    RelativeLayout main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        window = getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.main_background_color));

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        main = findViewById(R.id.main);

        handleKeyboardVisibility();

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_container, homeFragment, "home");
            transaction.add(R.id.fragment_container, myListingsFragment, "myListings").hide(myListingsFragment);
              transaction.add(R.id.fragment_container, profileFragment, "profile").hide(profileFragment);
              transaction.add(R.id.fragment_container, aiChatFragment, "aiChat").hide(aiChatFragment);
            transaction.commit();
            currentFragment = homeFragment;
        } else {
            homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("home");
            profileFragment = (ProfileFragment) getSupportFragmentManager().findFragmentByTag("profile");
            myListingsFragment = (MyListingsFragment) getSupportFragmentManager().findFragmentByTag("myListings");
            aiChatFragment = (AiChatFragment) getSupportFragmentManager().findFragmentByTag("aiChat");
            currentFragment = findVisibleFragment();
            if (currentFragment == null) {
                currentFragment = homeFragment;
                switchFragment(homeFragment);
            }
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment selectedFragment = null;

            if (itemId == R.id.navigation_home) {
                selectedFragment = homeFragment;
            } else if (itemId == R.id.navigation_Listing) {
                selectedFragment = myListingsFragment;
            }else if (itemId == R.id.navigation_aiChat) {
                selectedFragment = aiChatFragment;
            } else if (itemId == R.id.navigation_profile) {
                selectedFragment = profileFragment;
            }

            if (selectedFragment != null && selectedFragment != currentFragment) {
                switchFragment(selectedFragment);
                bottomNavigationView.setSelectedItemId(itemId);
            }
            return true;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void handleKeyboardVisibility() {
        main.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                main.getWindowVisibleDisplayFrame(r);
                int screenHeight = main.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                if (keypadHeight > screenHeight * 0.15) {
                    // Keyboard is visible
                    bottomNavigationView.setVisibility(View.GONE);
                } else {
                    bottomNavigationView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

        private void switchFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (currentFragment != null && currentFragment != fragment) {
            transaction.hide(currentFragment);
            Log.d("MainActivity", "Hiding fragment: " + currentFragment.getClass().getSimpleName());
        }

        if (fragment.isAdded()) {
            transaction.show(fragment);
            Log.d("MainActivity", "Showing fragment: " + fragment.getClass().getSimpleName());
        } else {
            transaction.add(R.id.fragment_container, fragment, fragment.getClass().getSimpleName());
            Log.d("MainActivity", "Adding fragment: " + fragment.getClass().getSimpleName());
        }

        transaction.commitNow();
        currentFragment = fragment;
        updateBottomNavigationViewSelection();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (currentFragment != homeFragment) {
            Log.d("MainActivity", "Navigating to home from: " + currentFragment.getClass().getSimpleName());
            switchFragment(homeFragment);
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        } else {
            Log.d("MainActivity", "Checking dialog state from home");
        }
    }

    private void updateBottomNavigationViewSelection() {
        if (currentFragment == homeFragment) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        }else if (currentFragment == myListingsFragment) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_Listing);
        } else if (currentFragment == profileFragment) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
        } else if (currentFragment == aiChatFragment) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_aiChat);
        }
        Log.d("MainActivity", "Updated BottomNavigationView to: " + bottomNavigationView.getSelectedItemId());
    }
    private Fragment findVisibleFragment() {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment != null && fragment.isVisible()) {
                Log.d("MainActivity", "Visible fragment found: " + fragment.getClass().getSimpleName());
                return fragment;
            }
        }
        Log.d("MainActivity", "No visible fragment found");
        return null;
    }
    public void hideBottomNavigation() {
        bottomNavigationView.animate()
                .translationY(bottomNavigationView.getHeight())
                .alpha(0f)
                .setDuration(200)
                .start();
    }

    public void showBottomNavigation() {
        bottomNavigationView.animate()
                .translationY(0)
                .alpha(1f)
                .setDuration(200)
                .start();
    }
}