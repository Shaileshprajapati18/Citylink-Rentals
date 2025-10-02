package com.example.citylinkrentals.Fragments;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.citylinkrentals.Activities.EditProfileActivity;
import com.example.citylinkrentals.Activities.FavoritePropertiesActivity;
import com.example.citylinkrentals.Activities.HelpAndSupportActivity;
import com.example.citylinkrentals.Activities.LoginActivity;
import com.example.citylinkrentals.Activities.MainActivity;
import com.example.citylinkrentals.Activities.MapActivity;
import com.example.citylinkrentals.Activities.PostPropertyActivity;
import com.example.citylinkrentals.Activities.PrivacyPolicyActivity;
import com.example.citylinkrentals.Activities.SearchPropertyActivity;
import com.example.citylinkrentals.R;
import com.example.citylinkrentals.model.User;
import com.example.citylinkrentals.model.UserProfileResponse;
import com.example.citylinkrentals.network.ApiService;
import com.example.citylinkrentals.network.RetrofitClient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "HomeFragment";

    // UI Components
    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private CoordinatorLayout coordinatorLayout;
    private NestedScrollView nestedScrollView;
    private MaterialButton btnPostProperty, btnSearchProperty, btnRequestCallback;
    private AutoCompleteTextView propertyTypeSpinner, cityEditText;
    private ImageView gifImageView;
    private NavigationView navigationView;
    private ImageView navProfileImage;
    private TextView navUserName, navUserEmail;
    private MaterialButton btnEditProfile, postPropertyBtn;
    private BottomNavigationView bottomNavigationView;

    // Category Cards
    private MaterialCardView roomCategoryCard, shopCategoryCard, pgCategoryCard, godownCategoryCard;
    private MaterialCardView buyHomeCard, rentHomeCard, pgColivingCard, buyPlotsLandCard,
            buyCommercialCard, leaseCommercialCard;
    private MaterialCardView indoreCard, gwaliorCard, bhopalCard, ujjainCard,
            sagarCard, dewasCard, satnaCard, ratlamCard;
    private MaterialCardView callSupportCard, whatsappSupportCard, chatSupportCard;
    private MaterialCardView article1Card, article2Card;
    private View feedbackPositive, feedbackNegative, viewAllArticles;

    // Firebase & API
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private ApiService apiService;
    private MainActivity mainActivity;

        // SharedPreferences keys
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_USER_IMAGE = "user_image";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    // Data
    private String[] propertyTypes = {"Sell", "Rent", "Paying Guest"};
    private String[] popularCities = {"Indore", "Gwalior", "Bhopal", "Ujjain", "Sagar", "Dewas", "Satna", "Ratlam"};
    private int currentHintIndex = 0;
    private Handler hintHandler = new Handler(Looper.getMainLooper());
    private Runnable hintRunnable;

    public HomeFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mainActivity = (MainActivity) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // Initialize Firebase & API
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        apiService = RetrofitClient.getApiService();

        Log.d(TAG, "HomeFragment created, User logged in: " + (currentUser != null));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initializeViews(view);
        setupPropertyTypeSpinner();
        setupNavigationDrawer();
        startHintRotation();
        setupClickListeners();

        // Load user profile data
        loadUserProfile();

        Log.d(TAG, "HomeFragment view created");
        return view;
    }

    private void initializeViews(View view) {
        coordinatorLayout = view.findViewById(R.id.main);
        nestedScrollView = view.findViewById(R.id.nestedScrollView);
        btnPostProperty = view.findViewById(R.id.btnPostProperty);
        btnSearchProperty = view.findViewById(R.id.btnSearchProperty);
        btnRequestCallback = view.findViewById(R.id.btnRequestCallback);
        propertyTypeSpinner = view.findViewById(R.id.propertyTypeSpinner);
        cityEditText = view.findViewById(R.id.cityEditText);
        gifImageView = view.findViewById(R.id.gifImageView);
        drawerLayout = view.findViewById(R.id.drawer_layout);
        toolbar = view.findViewById(R.id.toolbar);
        navigationView = view.findViewById(R.id.nav_view);
        bottomNavigationView = view.findViewById(R.id.bottom_navigation);

        // Initialize navigation header views
        if (navigationView != null) {
            View headerView = navigationView.getHeaderView(0);
            navProfileImage = headerView.findViewById(R.id.nav_profile_image);
            navUserName = headerView.findViewById(R.id.nav_user_name);
            navUserEmail = headerView.findViewById(R.id.nav_user_email);
            btnEditProfile = headerView.findViewById(R.id.btnEditProfile);
            postPropertyBtn = headerView.findViewById(R.id.post_property);
        }

        // Initialize category cards
        roomCategoryCard = view.findViewById(R.id.room_category_card);
        shopCategoryCard = view.findViewById(R.id.shop_category_card);
        pgCategoryCard = view.findViewById(R.id.pg_category_card);
        godownCategoryCard = view.findViewById(R.id.godown_category_card);

        buyHomeCard = view.findViewById(R.id.buy_home_card);
        rentHomeCard = view.findViewById(R.id.rent_home_card);
        pgColivingCard = view.findViewById(R.id.pg_coliving_card);
        buyPlotsLandCard = view.findViewById(R.id.buy_plots_land_card);
        buyCommercialCard = view.findViewById(R.id.buy_commercial_card);
        leaseCommercialCard = view.findViewById(R.id.lease_commercial_card);

        indoreCard = view.findViewById(R.id.indore_city_card);
        gwaliorCard = view.findViewById(R.id.gwalior_city_card);
        bhopalCard = view.findViewById(R.id.bhopal_city_card);
        ujjainCard = view.findViewById(R.id.ujjain_city_card);
        sagarCard = view.findViewById(R.id.sagar_city_card);
        dewasCard = view.findViewById(R.id.dewas_city_card);
        satnaCard = view.findViewById(R.id.satna_city_card);
        ratlamCard = view.findViewById(R.id.ratlam_city_card);

        callSupportCard = view.findViewById(R.id.call_support_card);
        whatsappSupportCard = view.findViewById(R.id.whatsapp_support_card);
        chatSupportCard = view.findViewById(R.id.chat_support_card);

        article1Card = view.findViewById(R.id.article1_card);
        article2Card = view.findViewById(R.id.article2_card);

        feedbackPositive = view.findViewById(R.id.feedback_positive);
        feedbackNegative = view.findViewById(R.id.feedback_negative);
        viewAllArticles = view.findViewById(R.id.view_all_articles);
    }

    private void setupPropertyTypeSpinner() {
        if (getContext() != null && propertyTypeSpinner != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    getContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    propertyTypes
            );
            propertyTypeSpinner.setAdapter(adapter);
            propertyTypeSpinner.setOnItemClickListener((parent, view, position, id) -> {
                String selectedType = propertyTypes[position];
                Log.d(TAG, "Property type selected: " + selectedType);
            });
        }
    }

    private void setupNavigationDrawer() {
        if (getActivity() != null && navigationView != null && drawerLayout != null && toolbar != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    getActivity(), drawerLayout, toolbar,
                    R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
            navigationView.setNavigationItemSelectedListener(this);
            setupHeaderViews();
            updateVersionNumber();
        } else {
            Log.e(TAG, "Navigation drawer setup failed");
            if (getContext() != null) {
                Toast.makeText(getContext(), "Navigation drawer not available", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateVersionNumber() {
        try {
            String versionName = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionName;
            MenuItem versionItem = navigationView.getMenu().findItem(R.id.nav_version);
            if (versionItem != null) {
                versionItem.setTitle("Version " + versionName);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting version: " + e.getMessage());
        }
    }

    private void setupHeaderViews() {
        // Edit Profile button
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), EditProfileActivity.class);
                startActivity(intent);
                if (drawerLayout != null) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
            });
        }

        // Profile image click
        if (navProfileImage != null) {
            navProfileImage.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), EditProfileActivity.class);
                startActivity(intent);
            });
        }

        // Post Property button in header
        if (postPropertyBtn != null) {
            postPropertyBtn.setOnClickListener(v -> {
                navigateToActivity(PostPropertyActivity.class);
                if (drawerLayout != null) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
            });
        }
    }

    /**
     * Load user profile from Firebase and API
     */
    private void loadUserProfile() {
        if (getContext() == null) return;

        Log.d(TAG, "Loading user profile...");

        // Check if user is logged in
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            Log.d(TAG, "User is logged in: " + currentUser.getUid());

            // Load from Firebase first (immediate data)
            loadProfileImageFromFirebase();
            loadFirebaseUserData();

            // Then load from API (more complete data)
            loadUserDataFromAPI();

            // Show logout option
            if (navigationView != null && navigationView.getMenu().findItem(R.id.nav_logout) != null) {
                navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
            }
        } else {
            Log.d(TAG, "User is not logged in, loading guest data");
            loadGuestUserData();

            // Hide logout option
            if (navigationView != null && navigationView.getMenu().findItem(R.id.nav_logout) != null) {
                navigationView.getMenu().findItem(R.id.nav_logout).setVisible(false);
            }
        }
    }

    /**
     * Load profile image from Firebase
     */
    private void loadProfileImageFromFirebase() {
        if (currentUser != null && currentUser.getPhotoUrl() != null && navProfileImage != null) {
            Glide.with(this)
                    .load(currentUser.getPhotoUrl())
                    .placeholder(R.drawable.profile_image_placeholder)
                    .error(R.drawable.profile_image_placeholder)
                    .circleCrop()
                    .into(navProfileImage);

            Log.d(TAG, "Loaded profile image from Firebase");
        } else if (navProfileImage != null) {
            navProfileImage.setImageResource(R.drawable.profile_image_placeholder);
        }
    }

    /**
     * Load basic user data from Firebase
     */
    private void loadFirebaseUserData() {
        if (currentUser == null) return;

        // Display Name
        String displayName = currentUser.getDisplayName();
        if (displayName != null && !displayName.trim().isEmpty()) {
            if (navUserName != null) navUserName.setText(displayName);
            saveToSharedPreferences(KEY_USER_NAME, displayName);
        }

        // Email
        String email = currentUser.getEmail();
        if (email != null && !email.trim().isEmpty()) {
            if (navUserEmail != null) navUserEmail.setText(email);
            saveToSharedPreferences(KEY_USER_EMAIL, email);
        }

        // Phone
        String phoneNumber = currentUser.getPhoneNumber();
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            saveToSharedPreferences(KEY_USER_PHONE, phoneNumber);
        }

        // Photo URL
        if (currentUser.getPhotoUrl() != null) {
            saveToSharedPreferences(KEY_USER_IMAGE, currentUser.getPhotoUrl().toString());
        }

        // Mark as logged in
        saveToSharedPreferences(KEY_IS_LOGGED_IN, true);

        Log.d(TAG, "Loaded Firebase user data - Name: " + displayName + ", Email: " + email);
    }

    /**
     * Load complete user data from API
     */
    private void loadUserDataFromAPI() {
        if (currentUser == null || getContext() == null) {
            Log.e(TAG, "Cannot load API data - user or context is null");
            return;
        }

        String firebaseUid = currentUser.getUid();
        Log.d(TAG, "Loading user data from API for UID: " + firebaseUid);

        Call<UserProfileResponse> call = apiService.getUserProfile(firebaseUid);
        call.enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                Log.d(TAG, "API Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    UserProfileResponse userProfileResponse = response.body();

                    if (userProfileResponse.getMessageBody() != null && !userProfileResponse.getMessageBody().isEmpty()) {
                        User userProfile = userProfileResponse.getMessageBody().get(0);
                        Log.d(TAG, "Successfully loaded user profile from API");
                        updateUIWithUserProfile(userProfile);
                    } else {
                        Log.e(TAG, "API response has empty messageBody");
                    }
                } else {
                    Log.e(TAG, "API response failed with code: " + response.code());
                    // Continue with Firebase data
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
                // Continue with Firebase data
            }
        });
    }

    /**
     * Update UI with API user profile data
     */
    private void updateUIWithUserProfile(User userProfile) {
        if (userProfile == null || getContext() == null) return;

        Log.d(TAG, "Updating UI with API user data");

        // Update username
        if (userProfile.getUsername() != null && !userProfile.getUsername().trim().isEmpty()) {
            if (navUserName != null) navUserName.setText(userProfile.getUsername());
            saveToSharedPreferences(KEY_USER_NAME, userProfile.getUsername());
        }

        // Update email
        if (userProfile.getEmail() != null && !userProfile.getEmail().trim().isEmpty()) {
            if (navUserEmail != null) navUserEmail.setText(userProfile.getEmail());
            saveToSharedPreferences(KEY_USER_EMAIL, userProfile.getEmail());
        }

        // Update phone
        if (userProfile.getPhoneNumber() != null && !userProfile.getPhoneNumber().trim().isEmpty()) {
            saveToSharedPreferences(KEY_USER_PHONE, userProfile.getPhoneNumber());
        }

        Log.d(TAG, "UI updated with API data");
    }

    /**
     * Load guest user data (not logged in)
     */
    private void loadGuestUserData() {
        if (navUserName != null) navUserName.setText("Guest User");
        if (navUserEmail != null) navUserEmail.setText("guest@example.com");
        if (navProfileImage != null) navProfileImage.setImageResource(R.drawable.profile_image_placeholder);

        saveToSharedPreferences(KEY_IS_LOGGED_IN, false);

        Log.d(TAG, "Loaded guest user data");
    }

    /**
     * Save data to SharedPreferences
     */
    private void saveToSharedPreferences(String key, String value) {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(key, value).apply();
    }

    private void saveToSharedPreferences(String key, boolean value) {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(key, value).apply();
    }

    private void startHintRotation() {
        hintRunnable = new Runnable() {
            @Override
            public void run() {
                if (cityEditText != null && getActivity() != null) {
                    cityEditText.setHint("e.g. " + popularCities[currentHintIndex]);
                    currentHintIndex = (currentHintIndex + 1) % popularCities.length;
                    hintHandler.postDelayed(this, 2000);
                }
            }
        };
        hintHandler.post(hintRunnable);
    }

    private void setupClickListeners() {
        if (gifImageView != null && getActivity() != null) {
            gifImageView.setOnClickListener(v -> {
                Log.d(TAG, "Map view clicked");
                navigateToActivity(MapActivity.class);
            });
            gifImageView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(800)
                    .setInterpolator(new OvershootInterpolator(2f))
                    .setStartDelay(300)
                    .start();
        }

        if (btnPostProperty != null && getActivity() != null) {
            btnPostProperty.setOnClickListener(v -> {
                Log.d(TAG, "Post Property clicked");
                navigateToActivity(PostPropertyActivity.class);
            });
        }

        if (btnSearchProperty != null && getActivity() != null) {
            btnSearchProperty.setOnClickListener(v -> {
                if (validateSearchInputs()) {
                    performMainSearch();
                }
            });
        }

        if (btnRequestCallback != null && getContext() != null) {
            btnRequestCallback.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Callback request sent! We'll contact you soon.", Toast.LENGTH_LONG).show();
            });
        }

        setupCategoryCardListeners();
        setupLookingForCardListeners();
        setupPopularCitiesListeners();
        setupSupportCardListeners();
        setupArticleCardListeners();
        setupFeedbackListeners();
    }

    private void setupCategoryCardListeners() {
        View.OnClickListener categoryListener = v -> {
            String category = "";
            if (v.getId() == R.id.room_category_card) category = "Room";
            else if (v.getId() == R.id.shop_category_card) category = "Shop";
            else if (v.getId() == R.id.pg_category_card) category = "P.G";
            else if (v.getId() == R.id.godown_category_card) category = "Godown";
            if (!category.isEmpty()) navigateToSearchWithCategory(category);
        };

        if (roomCategoryCard != null) roomCategoryCard.setOnClickListener(categoryListener);
        if (shopCategoryCard != null) shopCategoryCard.setOnClickListener(categoryListener);
        if (pgCategoryCard != null) pgCategoryCard.setOnClickListener(categoryListener);
        if (godownCategoryCard != null) godownCategoryCard.setOnClickListener(categoryListener);
    }

    private void setupLookingForCardListeners() {
        View.OnClickListener lookingForListener = v -> {
            String propertyCategory = "";
            String saleType = "";
            if (v.getId() == R.id.buy_home_card) {
                propertyCategory = "Residential";
                saleType = "Sell";
            } else if (v.getId() == R.id.rent_home_card) {
                propertyCategory = "Residential";
                saleType = "Rent";
            } else if (v.getId() == R.id.pg_coliving_card) {
                navigateToSearchWithCategory("P.G");
                return;
            } else if (v.getId() == R.id.buy_plots_land_card) {
                propertyCategory = "Plot";
                saleType = "Sell";
            } else if (v.getId() == R.id.buy_commercial_card) {
                propertyCategory = "Commercial";
                saleType = "Sell";
            } else if (v.getId() == R.id.lease_commercial_card) {
                propertyCategory = "Commercial";
                saleType = "Rent";
            }
            if (!propertyCategory.isEmpty() && !saleType.isEmpty()) {
                navigateToSearchWithFilter(propertyCategory, saleType);
            }
        };

        if (buyHomeCard != null) buyHomeCard.setOnClickListener(lookingForListener);
        if (rentHomeCard != null) rentHomeCard.setOnClickListener(lookingForListener);
        if (pgColivingCard != null) pgColivingCard.setOnClickListener(lookingForListener);
        if (buyPlotsLandCard != null) buyPlotsLandCard.setOnClickListener(lookingForListener);
        if (buyCommercialCard != null) buyCommercialCard.setOnClickListener(lookingForListener);
        if (leaseCommercialCard != null) leaseCommercialCard.setOnClickListener(lookingForListener);
    }

    private void setupPopularCitiesListeners() {
        View.OnClickListener cityListener = v -> {
            String city = "";
            if (v.getId() == R.id.indore_city_card) city = "Indore";
            else if (v.getId() == R.id.gwalior_city_card) city = "Gwalior";
            else if (v.getId() == R.id.bhopal_city_card) city = "Bhopal";
            else if (v.getId() == R.id.ujjain_city_card) city = "Ujjain";
            else if (v.getId() == R.id.sagar_city_card) city = "Sagar";
            else if (v.getId() == R.id.dewas_city_card) city = "Dewas";
            else if (v.getId() == R.id.satna_city_card) city = "Satna";
            else if (v.getId() == R.id.ratlam_city_card) city = "Ratlam";
            if (!city.isEmpty()) navigateToSearchWithCity(city);
        };

        if (indoreCard != null) indoreCard.setOnClickListener(cityListener);
        if (gwaliorCard != null) gwaliorCard.setOnClickListener(cityListener);
        if (bhopalCard != null) bhopalCard.setOnClickListener(cityListener);
        if (ujjainCard != null) ujjainCard.setOnClickListener(cityListener);
        if (sagarCard != null) sagarCard.setOnClickListener(cityListener);
        if (dewasCard != null) dewasCard.setOnClickListener(cityListener);
        if (satnaCard != null) satnaCard.setOnClickListener(cityListener);
        if (ratlamCard != null) ratlamCard.setOnClickListener(cityListener);
    }

    private void setupSupportCardListeners() {
        if (callSupportCard != null) {
            callSupportCard.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(android.net.Uri.parse("tel:1800-41-99099"));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Unable to make call", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error making phone call", e);
                }
            });
        }

        if (whatsappSupportCard != null) {
            whatsappSupportCard.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(android.net.Uri.parse("https://wa.me/919876543210"));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "WhatsApp not installed", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error opening WhatsApp", e);
                }
            });
        }

        if (chatSupportCard != null) {
            chatSupportCard.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Chat support will be available soon!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupArticleCardListeners() {
        View.OnClickListener articleListener = v -> {
            Toast.makeText(getContext(), "Opening article...", Toast.LENGTH_SHORT).show();
        };

        if (article1Card != null) article1Card.setOnClickListener(articleListener);
        if (article2Card != null) article2Card.setOnClickListener(articleListener);
        if (viewAllArticles != null) {
            viewAllArticles.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Opening all articles...", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupFeedbackListeners() {
        if (feedbackPositive != null) {
            feedbackPositive.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Thank you for your positive feedback!", Toast.LENGTH_LONG).show();
            });
        }
        if (feedbackNegative != null) {
            feedbackNegative.setOnClickListener(v -> {
                Toast.makeText(getContext(), "We'll work to improve your experience.", Toast.LENGTH_LONG).show();
            });
        }
    }

    private boolean validateSearchInputs() {
        if (propertyTypeSpinner == null || cityEditText == null) return false;

        String propertyType = propertyTypeSpinner.getText().toString().trim();
        String city = cityEditText.getText().toString().trim();

        if (propertyType.isEmpty()) {
            propertyTypeSpinner.requestFocus();
            return false;
        }
        if (city.isEmpty()) {
            cityEditText.setError("Please enter a city");
            cityEditText.requestFocus();
            return false;
        }
        if (city.length() < 2) {
            cityEditText.setError("City name too short");
            cityEditText.requestFocus();
            return false;
        }
        return true;
    }

    private void performMainSearch() {
        if (getActivity() == null) return;

        String propertyType = propertyTypeSpinner.getText().toString().trim();
        String city = cityEditText.getText().toString().trim();

        Intent intent = new Intent(getActivity(), SearchPropertyActivity.class);
        if (propertyType.equals("Sell") || propertyType.equals("Rent")) {
            intent.putExtra("search_category", propertyType);
        } else if (propertyType.equals("Paying Guest")) {
            intent.putExtra("property_type", "P.G");
            intent.putExtra("auto_select_chip", "P.G");
        }
        intent.putExtra("city", city);
        intent.putExtra("search_query", city);
        startActivity(intent);
        addSlideTransition();
        Log.d(TAG, "Main search performed - Type: " + propertyType + ", City: " + city);
    }

    private void navigateToSearchWithCategory(String category) {
        if (getActivity() == null) return;

        Log.d(TAG, "Category selected: " + category);
        Intent intent = new Intent(getActivity(), SearchPropertyActivity.class);
        intent.putExtra("property_type", category);
        intent.putExtra("auto_select_chip", category);

        String city = cityEditText != null ? cityEditText.getText().toString().trim() : "";
        if (!city.isEmpty()) {
            intent.putExtra("city", city);
            intent.putExtra("search_query", city);
        }
        startActivity(intent);
        addSlideTransition();
    }

    private void navigateToSearchWithFilter(String propertyCategory, String saleType) {
        if (getActivity() == null) return;

        Log.d(TAG, "Filter selected - Category: " + propertyCategory + ", Type: " + saleType);
        Intent intent = new Intent(getActivity(), SearchPropertyActivity.class);

        if (propertyCategory.equals("Residential")) {
            intent.putExtra("auto_select_chip", "Residential");
        } else if (propertyCategory.equals("Commercial")) {
            intent.putExtra("auto_select_chip", "Commercial");
        } else if (propertyCategory.equals("Plot")) {
            intent.putExtra("property_type", "Plot");
            intent.putExtra("auto_select_chip", "All");
        }
        intent.putExtra("search_category", saleType);

        String city = cityEditText != null ? cityEditText.getText().toString().trim() : "";
        if (!city.isEmpty()) {
            intent.putExtra("city", city);
            intent.putExtra("search_query", city);
        }
        startActivity(intent);
        addSlideTransition();
    }

    private void navigateToSearchWithCity(String city) {
        if (getActivity() == null) return;

        Log.d(TAG, "City selected: " + city);
        Intent intent = new Intent(getActivity(), SearchPropertyActivity.class);
        intent.putExtra("city", city);
        intent.putExtra("search_query", city);
        intent.putExtra("auto_select_chip", "All");
        startActivity(intent);
        addSlideTransition();
    }

    private void navigateToActivity(Class<?> activityClass) {
        if (getActivity() == null) return;
        Intent intent = new Intent(getActivity(), activityClass);
        startActivity(intent);
        addSlideTransition();
    }

    private void addSlideTransition() {
        if (getActivity() != null) {
            getActivity().overridePendingTransition(
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right
            );
        }
    }

    private void showLogoutDialog() {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    performLogout();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void performLogout() {
        if (getContext() == null) return;

        // Sign out from Firebase
        mAuth.signOut();

        // Clear SharedPreferences
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().clear().apply();

        // Also clear app settings
        SharedPreferences appPrefs = getContext().getSharedPreferences("app_settings", MODE_PRIVATE);
        appPrefs.edit().clear().apply();

        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate to login activity
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();

        Log.d(TAG, "User logged out successfully");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
        } else if (itemId == R.id.nav_my_properties) {
            navigateToActivity(SearchPropertyActivity.class);
        } else if (itemId == R.id.nav_favorites) {
            navigateToActivity(FavoritePropertiesActivity.class);
        } else if (itemId == R.id.nav_privacy_policy) {
            navigateToActivity(PrivacyPolicyActivity.class);
        } else if (itemId == R.id.nav_help) {
            navigateToActivity(HelpAndSupportActivity.class);
        } else if (itemId == R.id.nav_logout) {
            showLogoutDialog();
        }

        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Handle back button press
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            drawerLayout.closeDrawer(GravityCompat.START);
                        } else {
                            setEnabled(false);
                            requireActivity().onBackPressed();
                        }
                    }
                }
        );

        Log.d(TAG, "HomeFragment view created and configured");
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter("com.example.citylinkrentals.PROFILE_UPDATED");
        if (getContext() != null) {
            LocalBroadcastManager.getInstance(getContext())
                    .registerReceiver(profileUpdateReceiver, filter);
        }

        loadUserProfile();
        Log.d(TAG, "HomeFragment resumed");
    }

    @Override
    public void onPause() {
        super.onPause();

        // Unregister receiver
        if (getContext() != null) {
            LocalBroadcastManager.getInstance(getContext())
                    .unregisterReceiver(profileUpdateReceiver);
        }

        if (hintHandler != null && hintRunnable != null) {
            hintHandler.removeCallbacks(hintRunnable);
        }

        Log.d(TAG, "HomeFragment paused");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Clean up handler
        if (hintHandler != null && hintRunnable != null) {
            hintHandler.removeCallbacks(hintRunnable);
        }

        // Nullify all views to prevent memory leaks
        coordinatorLayout = null;
        nestedScrollView = null;
        btnPostProperty = null;
        btnSearchProperty = null;
        btnRequestCallback = null;
        propertyTypeSpinner = null;
        cityEditText = null;
        gifImageView = null;
        drawerLayout = null;
        toolbar = null;
        bottomNavigationView = null;
        navigationView = null;
        navProfileImage = null;
        navUserName = null;
        navUserEmail = null;
        btnEditProfile = null;
        postPropertyBtn = null;
        roomCategoryCard = null;
        shopCategoryCard = null;
        pgCategoryCard = null;
        godownCategoryCard = null;
        buyHomeCard = null;
        rentHomeCard = null;
        pgColivingCard = null;
        buyPlotsLandCard = null;
        buyCommercialCard = null;
        leaseCommercialCard = null;
        indoreCard = null;
        gwaliorCard = null;
        bhopalCard = null;
        ujjainCard = null;
        sagarCard = null;
        dewasCard = null;
        satnaCard = null;
        ratlamCard = null;
        callSupportCard = null;
        whatsappSupportCard = null;
        chatSupportCard = null;
        article1Card = null;
        article2Card = null;
        feedbackPositive = null;
        feedbackNegative = null;
        viewAllArticles = null;

        Log.d(TAG, "HomeFragment view destroyed");
    }

    /**
     * Public method to refresh user profile (can be called from MainActivity)
     */
    public void refreshUserProfile() {
        loadUserProfile();
        Log.d(TAG, "User profile refreshed");
    }

    private BroadcastReceiver profileUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Profile update broadcast received");
            loadUserProfile();
        }
    };

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
        Log.d(TAG, "HomeFragment detached");
    }
}