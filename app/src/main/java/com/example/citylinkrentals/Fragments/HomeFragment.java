package com.example.citylinkrentals.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.example.citylinkrentals.Activities.MainActivity;
import com.example.citylinkrentals.Activities.PostPropertyActivity;
import com.example.citylinkrentals.Activities.SearchPropertyActivity;
import com.example.citylinkrentals.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    // UI Components
    private CoordinatorLayout coordinatorLayout;
    private NestedScrollView nestedScrollView;
    private MaterialButton btnPostProperty, btnSearchProperty, btnRequestCallback;
    private AutoCompleteTextView propertyTypeSpinner;
    private TextInputEditText cityEditText;

    // Category Cards
    private MaterialCardView roomCategoryCard, shopCategoryCard, pgCategoryCard, godownCategoryCard;

    // What are you looking for cards
    private MaterialCardView buyHomeCard, rentHomeCard, pgColivingCard, buyPlotsLandCard,
            buyCommercialCard, leaseCommercialCard;

    // Popular cities cards
    private MaterialCardView delhiCityCard, mumbaiCityCard, bangaloreCityCard, hyderabadCityCard;

    // Support cards
    private MaterialCardView callSupportCard, whatsappSupportCard, chatSupportCard;

    // Article cards
    private MaterialCardView article1Card, article2Card;

    // Feedback
    private View feedbackPositive, feedbackNegative, viewAllArticles;

    private MainActivity mainActivity;

    // Updated property types with Sell, Rent, Paying Guest
    private String[] propertyTypes = {"Sell", "Rent", "Paying Guest"};

    // Popular cities
    private String[] popularCities = {"Delhi", "Mumbai", "Bangalore", "Hyderabad", "Chennai", "Pune", "Kolkata", "Ahmedabad"};

    public HomeFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActivity() instanceof MainActivity) {
            mainActivity = (MainActivity) getActivity();
        }
        Log.d(TAG, "HomeFragment created");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initializeViews(view);
        setupPropertyTypeSpinner();
        setupClickListeners();
        setupScrollBehavior();

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

        // Category cards
        roomCategoryCard = view.findViewById(R.id.room_category_card);
        shopCategoryCard = view.findViewById(R.id.shop_category_card);
        pgCategoryCard = view.findViewById(R.id.pg_category_card);
        godownCategoryCard = view.findViewById(R.id.godown_category_card);

        // What are you looking for cards
        buyHomeCard = view.findViewById(R.id.buy_home_card);
        rentHomeCard = view.findViewById(R.id.rent_home_card);
        pgColivingCard = view.findViewById(R.id.pg_coliving_card);
        buyPlotsLandCard = view.findViewById(R.id.buy_plots_land_card);
        buyCommercialCard = view.findViewById(R.id.buy_commercial_card);
        leaseCommercialCard = view.findViewById(R.id.lease_commercial_card);

        // Popular cities cards
        delhiCityCard = view.findViewById(R.id.delhi_city_card);
        mumbaiCityCard = view.findViewById(R.id.mumbai_city_card);
        bangaloreCityCard = view.findViewById(R.id.bangalore_city_card);
        hyderabadCityCard = view.findViewById(R.id.hyderabad_city_card);

        // Support cards
        callSupportCard = view.findViewById(R.id.call_support_card);
        whatsappSupportCard = view.findViewById(R.id.whatsapp_support_card);
        chatSupportCard = view.findViewById(R.id.chat_support_card);

        // Article cards
        article1Card = view.findViewById(R.id.article1_card);
        article2Card = view.findViewById(R.id.article2_card);

        // Feedback
        feedbackPositive = view.findViewById(R.id.feedback_positive);
        feedbackNegative = view.findViewById(R.id.feedback_negative);
        viewAllArticles = view.findViewById(R.id.view_all_articles);
    }

    private void setupPropertyTypeSpinner() {
        if (getContext() != null) {
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

    private void setupClickListeners() {
        // Post Property Button
        btnPostProperty.setOnClickListener(v -> {
            Log.d(TAG, "Post Property clicked");
            Intent intent = new Intent(getActivity(), PostPropertyActivity.class);
            startActivity(intent);
            addSlideTransition();
        });

        // Main Search Button
        btnSearchProperty.setOnClickListener(v -> {
            if (validateSearchInputs()) {
                performMainSearch();
            }
        });

        // Property Category Cards
        setupCategoryCardListeners();

        // What are you looking for Cards
        setupLookingForCardListeners();

        // Popular Cities Cards
        setupPopularCitiesListeners();

        // Support Cards
        setupSupportCardListeners();

        // Article Cards
        setupArticleCardListeners();

        // Feedback
        setupFeedbackListeners();

        // Request Callback
        btnRequestCallback.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Callback request sent! We'll contact you soon.", Toast.LENGTH_LONG).show();
        });
    }

    private void setupCategoryCardListeners() {
        if (roomCategoryCard != null) {
            roomCategoryCard.setOnClickListener(v -> navigateToSearchWithCategory("Room"));
        }
        if (shopCategoryCard != null) {
            shopCategoryCard.setOnClickListener(v -> navigateToSearchWithCategory("Shop"));
        }
        if (pgCategoryCard != null) {
            pgCategoryCard.setOnClickListener(v -> navigateToSearchWithCategory("P.G"));
        }
        if (godownCategoryCard != null) {
            godownCategoryCard.setOnClickListener(v -> navigateToSearchWithCategory("Godown"));
        }
    }

    private void setupLookingForCardListeners() {
        if (buyHomeCard != null) {
            buyHomeCard.setOnClickListener(v -> navigateToSearchWithFilter("Residential", "Sell"));
        }
        if (rentHomeCard != null) {
            rentHomeCard.setOnClickListener(v -> navigateToSearchWithFilter("Residential", "Rent"));
        }
        if (pgColivingCard != null) {
            pgColivingCard.setOnClickListener(v -> navigateToSearchWithCategory("P.G"));
        }
        if (buyPlotsLandCard != null) {
            buyPlotsLandCard.setOnClickListener(v -> navigateToSearchWithFilter("Plot", "Sell"));
        }
        if (buyCommercialCard != null) {
            buyCommercialCard.setOnClickListener(v -> navigateToSearchWithFilter("Commercial", "Sell"));
        }
        if (leaseCommercialCard != null) {
            leaseCommercialCard.setOnClickListener(v -> navigateToSearchWithFilter("Commercial", "Rent"));
        }
    }

    private void setupPopularCitiesListeners() {
        if (delhiCityCard != null) {
            delhiCityCard.setOnClickListener(v -> navigateToSearchWithCity("Delhi"));
        }
        if (mumbaiCityCard != null) {
            mumbaiCityCard.setOnClickListener(v -> navigateToSearchWithCity("Mumbai"));
        }
        if (bangaloreCityCard != null) {
            bangaloreCityCard.setOnClickListener(v -> navigateToSearchWithCity("Bangalore"));
        }
        if (hyderabadCityCard != null) {
            hyderabadCityCard.setOnClickListener(v -> navigateToSearchWithCity("Hyderabad"));
        }
    }

    private void setupSupportCardListeners() {
        if (callSupportCard != null) {
            callSupportCard.setOnClickListener(v -> {
                // Make phone call
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(android.net.Uri.parse("tel:1800-41-99099"));
                startActivity(intent);
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
        if (article1Card != null) {
            article1Card.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Opening article...", Toast.LENGTH_SHORT).show();
                // You can open article in WebView or browser
            });
        }

        if (article2Card != null) {
            article2Card.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Opening article...", Toast.LENGTH_SHORT).show();
                // You can open article in WebView or browser
            });
        }

        if (viewAllArticles != null) {
            viewAllArticles.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Opening all articles...", Toast.LENGTH_SHORT).show();
                // Navigate to articles section
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
                Toast.makeText(getContext(), "We'll work to improve your experience. Please contact support for specific issues.", Toast.LENGTH_LONG).show();
            });
        }
    }

    private boolean validateSearchInputs() {
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
        String propertyType = propertyTypeSpinner.getText().toString().trim();
        String city = cityEditText.getText().toString().trim();

        Intent intent = new Intent(getActivity(), SearchPropertyActivity.class);

        // Map property types to categories
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
        Log.d(TAG, "Category selected: " + category);

        Intent intent = new Intent(getActivity(), SearchPropertyActivity.class);
        intent.putExtra("property_type", category);
        intent.putExtra("auto_select_chip", category);

        // Auto-populate the search if city is entered
        String city = cityEditText.getText().toString().trim();
        if (!city.isEmpty()) {
            intent.putExtra("city", city);
            intent.putExtra("search_query", city);
        }

        startActivity(intent);
        addSlideTransition();
    }

    private void navigateToSearchWithFilter(String propertyCategory, String saleType) {
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

        // Auto-populate the search if city is entered
        String city = cityEditText.getText().toString().trim();
        if (!city.isEmpty()) {
            intent.putExtra("city", city);
            intent.putExtra("search_query", city);
        }

        startActivity(intent);
        addSlideTransition();
    }

    private void navigateToSearchWithCity(String city) {
        Log.d(TAG, "City selected: " + city);

        Intent intent = new Intent(getActivity(), SearchPropertyActivity.class);
        intent.putExtra("city", city);
        intent.putExtra("search_query", city);
        intent.putExtra("auto_select_chip", "All");

        startActivity(intent);
        addSlideTransition();
    }

    private void setupScrollBehavior() {
        if (coordinatorLayout != null && mainActivity != null) {
            coordinatorLayout.setOnScrollChangeListener(
                    (CoordinatorLayout.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

                        if (scrollY > oldScrollY && scrollY > 100) {
                            mainActivity.hideBottomNavigation();
                        } else if (scrollY < oldScrollY) {
                            mainActivity.showBottomNavigation();
                        }

                        if (Math.abs(scrollY - oldScrollY) > 50) {
                            Log.d(TAG, "Scroll detected - Y: " + scrollY + ", Direction: " +
                                    (scrollY > oldScrollY ? "Down" : "Up"));
                        }
                    }
            );
        }
    }

    private void addSlideTransition() {
        if (getActivity() != null) {
            getActivity().overridePendingTransition(
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right
            );
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mainActivity != null) {
            mainActivity.showBottomNavigation();
        }

        Log.d(TAG, "HomeFragment view created and configured");
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mainActivity != null) {
            mainActivity.showBottomNavigation();
        }
        Log.d(TAG, "HomeFragment resumed");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "HomeFragment paused");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        coordinatorLayout = null;
        nestedScrollView = null;
        btnPostProperty = null;
        btnSearchProperty = null;
        propertyTypeSpinner = null;
        cityEditText = null;

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

        delhiCityCard = null;
        mumbaiCityCard = null;
        bangaloreCityCard = null;
        hyderabadCityCard = null;

        mainActivity = null;
        Log.d(TAG, "HomeFragment view destroyed");
    }
}