package com.example.citylinkrentals.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

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
    private MaterialButton btnPostProperty, btnSearchProperty;
    private AutoCompleteTextView propertyTypeSpinner;
    private TextInputEditText cityEditText;
    private MaterialCardView[] categoryCards;

    private MainActivity mainActivity;

    private String[] propertyTypes = {"Room", "Shop", "PG", "Godown","Flat", "Apartment", "House"};

    public HomeFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get MainActivity reference
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
        setupCategoryCards(view);

        Log.d(TAG, "HomeFragment view created");
        return view;
    }

    private void initializeViews(View view) {
        coordinatorLayout = view.findViewById(R.id.main);
        nestedScrollView = view.findViewById(R.id.nestedScrollView);
        btnPostProperty = view.findViewById(R.id.btnPostProperty);
        btnSearchProperty = view.findViewById(R.id.btnSearchProperty);
        propertyTypeSpinner = view.findViewById(R.id.propertyTypeSpinner);
        cityEditText = view.findViewById(R.id.cityEditText);
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
                // You can add analytics or other logic here
            });
        }
    }

    private void setupClickListeners() {

        btnPostProperty.setOnClickListener(v -> {
            Log.d(TAG, "Post Property clicked");
            Intent intent = new Intent(getActivity(), PostPropertyActivity.class);
            startActivity(intent);

            if (getActivity() != null) {
                getActivity().overridePendingTransition(
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                );
            }
        });

        btnSearchProperty.setOnClickListener(v -> {
            Log.d(TAG, "Search Property clicked");

            String selectedPropertyType = propertyTypeSpinner.getText().toString().trim();
            String selectedCity = cityEditText.getText().toString().trim();

            Intent intent = new Intent(getActivity(), SearchPropertyActivity.class);

            if (!selectedPropertyType.isEmpty()) {
                intent.putExtra("property_type", selectedPropertyType);
            }
            if (!selectedCity.isEmpty()) {
                intent.putExtra("city", selectedCity);
            }

            startActivity(intent);

            if (getActivity() != null) {
                getActivity().overridePendingTransition(
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                );
            }
        });
    }

    private void setupCategoryCards(View view) {

        MaterialCardView roomCard = view.findViewById(R.id.room_category_card);
        MaterialCardView shopCard = view.findViewById(R.id.shop_category_card);
        MaterialCardView pgCard = view.findViewById(R.id.pg_category_card);
        MaterialCardView godownCard = view.findViewById(R.id.godown_category_card);

        if (roomCard != null) {
            roomCard.setOnClickListener(v -> onCategorySelected("Room"));
        }
        if (shopCard != null) {
            shopCard.setOnClickListener(v -> onCategorySelected("Shop"));
        }
        if (pgCard != null) {
            pgCard.setOnClickListener(v -> onCategorySelected("PG"));
        }
        if (godownCard != null) {
            godownCard.setOnClickListener(v -> onCategorySelected("Godown"));
        }
    }

    private void onCategorySelected(String category) {
        Log.d(TAG, "Category selected: " + category);

        propertyTypeSpinner.setText(category, false);

        Intent intent = new Intent(getActivity(), SearchPropertyActivity.class);
        intent.putExtra("property_type", category);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().overridePendingTransition(
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right
            );
        }
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
        mainActivity = null;
        Log.d(TAG, "HomeFragment view destroyed");
    }

    private boolean validateSearchInputs() {
        String city = cityEditText.getText().toString().trim();

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

    // Enhanced search with validation
    private void performSearch() {
        if (!validateSearchInputs()) {
            return;
        }

        String propertyType = propertyTypeSpinner.getText().toString().trim();
        String city = cityEditText.getText().toString().trim();

        Intent intent = new Intent(getActivity(), SearchPropertyActivity.class);
        intent.putExtra("property_type", propertyType);
        intent.putExtra("city", city);

        startActivity(intent);

        if (getActivity() != null) {
            getActivity().overridePendingTransition(
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right
            );
        }

        Log.d(TAG, "Search performed - Type: " + propertyType + ", City: " + city);
    }
}