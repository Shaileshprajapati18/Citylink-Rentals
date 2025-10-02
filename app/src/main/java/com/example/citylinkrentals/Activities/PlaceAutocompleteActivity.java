package com.example.citylinkrentals.Activities;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.citylinkrentals.Adapter.PlacePredictionAdapter;
import com.example.citylinkrentals.R;
import com.example.citylinkrentals.model.MockPrediction;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlaceAutocompleteActivity extends AppCompatActivity
        implements PlacePredictionAdapter.OnPlaceClickListener {

    private TextInputEditText searchEditText;
    private RecyclerView predictionsRecyclerView;
    private ExecutorService executorService;
    private Handler mainHandler;
    private ProgressBar progressBar;

    private PlacesClient placesClient;
    private PlacePredictionAdapter adapter;
    private AutocompleteSessionToken sessionToken;

    private Handler searchHandler;
    private static final int SEARCH_DELAY_MS = 300;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_autocomplete);

        searchEditText = findViewById(R.id.search_edit_text);
        predictionsRecyclerView = findViewById(R.id.predictions_recycler_view);
        progressBar = findViewById(R.id.progress_bar);

        searchHandler = new Handler(Looper.getMainLooper());
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        sessionToken = AutocompleteSessionToken.newInstance();

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_map_api_key));
        }
        placesClient = Places.createClient(this);

        adapter = new PlacePredictionAdapter(this);
        predictionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        predictionsRecyclerView.setAdapter(adapter);


        setupSearch();
    }


    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchHandler.removeCallbacksAndMessages(null);

                if (s.length() > 0) {
                    searchHandler.postDelayed(() -> searchPlaces(s.toString()), SEARCH_DELAY_MS);
                } else {
                    adapter.updatePredictions(new ArrayList<>());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    private void searchPlaces(String query) {
        if (query.trim().isEmpty()) return;

        progressBar.setVisibility(View.VISIBLE);

        executorService.execute(() -> {
            List<Address> addresses = new ArrayList<>();
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                addresses = geocoder.getFromLocationName(query, 5);
            } catch (Exception e) {
                Log.e(TAG, "Geocoding failed", e);
            }

            List<MockPrediction> mockPredictions = new ArrayList<>();
            for (Address address : addresses) {
                mockPredictions.add(new MockPrediction(address));
            }

            mainHandler.post(() -> {
                progressBar.setVisibility(View.GONE);
                adapter.updateMockPredictions(mockPredictions);
            });
        });

    }

    @Override
    public void onPlaceClick(AutocompletePrediction prediction) {

    }
}