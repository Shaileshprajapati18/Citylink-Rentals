package com.example.citylinkrentals.Activities;

import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.citylinkrentals.Adapter.PropertyAdapter;
import com.example.citylinkrentals.R;
import com.example.citylinkrentals.model.Property;
import com.example.citylinkrentals.model.FavoriteManager;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class FavoritePropertiesActivity extends AppCompatActivity
        implements PropertyAdapter.OnFavoriteChangeListener {

    private RecyclerView favoritesRecyclerView;
    private PropertyAdapter favoriteAdapter;
    private FavoriteManager favoriteManager;
    private TextView emptyStateText;
    private MaterialButton explorePropertiesButton;
    private View emptyStateLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_properties);

        favoriteManager = FavoriteManager.getInstance(this);

        setupToolbar();
        initializeViews();
        setupRecyclerView();
        loadFavoriteProperties();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.main_background_color, getTheme()));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decorView = window.getDecorView();
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Favorite Properties");
        }
    }

    private void initializeViews() {
        favoritesRecyclerView = findViewById(R.id.favorites_recycler_view);
        emptyStateLayout = findViewById(R.id.empty_state_layout);
        emptyStateText = findViewById(R.id.empty_state_text);
        explorePropertiesButton = findViewById(R.id.explore_properties_button);

        // Set up empty state button click
        explorePropertiesButton.setOnClickListener(v -> {
            // Navigate back or to property search
            finish();
        });
    }

    private void setupRecyclerView() {
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        favoriteAdapter = new PropertyAdapter(this, null);
        favoriteAdapter.setOnFavoriteChangeListener(this);
        favoritesRecyclerView.setAdapter(favoriteAdapter);
    }

    private void loadFavoriteProperties() {
        List<Property> favoriteProperties = favoriteManager.getFavorites();

        if (favoriteProperties.isEmpty()) {
            showEmptyState();
        } else {
            showFavoritesList();
            favoriteAdapter.updateList(favoriteProperties);

            // Update toolbar title with count
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Favorites (" + favoriteProperties.size() + ")");
            }
        }
    }

    private void showEmptyState() {
        emptyStateLayout.setVisibility(View.VISIBLE);
        favoritesRecyclerView.setVisibility(View.GONE);
        emptyStateText.setText("No favorite properties yet!\nStart exploring and save properties you like.");
    }

    private void showFavoritesList() {
        emptyStateLayout.setVisibility(View.GONE);
        favoritesRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFavoriteChanged() {
        // Reload the list when a favorite is removed
        loadFavoriteProperties();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload favorites when activity resumes
        loadFavoriteProperties();
    }
}