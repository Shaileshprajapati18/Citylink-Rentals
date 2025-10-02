package com.example.citylinkrentals.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.citylinkrentals.Activities.PostPropertyActivity;
import com.example.citylinkrentals.Activities.PropertyDetailsActivity;
import com.example.citylinkrentals.R;
import com.example.citylinkrentals.Adapter.MyPropertiesAdapter;
import com.example.citylinkrentals.model.Property;
import com.example.citylinkrentals.model.PropertyListResponse;
import com.example.citylinkrentals.model.ResponseDTO;
import com.example.citylinkrentals.network.ApiService;
import com.example.citylinkrentals.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyListingsFragment extends Fragment implements MyPropertiesAdapter.OnPropertyActionListener {

    private static final String TAG = "MyListingsFragment";
    private static final int EDIT_PROPERTY_REQUEST_CODE = 1001;

    // UI Components
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView propertiesRecyclerView;
    private LinearLayout loadingLayout, emptyStateLayout, errorStateLayout;
    private MaterialButton addPropertyButton, emptyAddPropertyButton, retryButton;
    private TextView errorMessage;

    // Data
    private MyPropertiesAdapter propertiesAdapter;
    private List<Property> propertiesList;
    private ApiService apiService;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_listings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();
        initializeData();
        loadUserProperties();
    }

    private void initializeViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        propertiesRecyclerView = view.findViewById(R.id.properties_recycler_view);

        loadingLayout = view.findViewById(R.id.loading_layout);
        emptyStateLayout = view.findViewById(R.id.empty_state_layout);
        errorStateLayout = view.findViewById(R.id.error_state_layout);

        addPropertyButton = view.findViewById(R.id.add_property_button);
        emptyAddPropertyButton = view.findViewById(R.id.empty_add_property_button);
        retryButton = view.findViewById(R.id.retry_button);

        errorMessage = view.findViewById(R.id.error_message);
    }

    private void setupRecyclerView() {
        propertiesList = new ArrayList<>();
        propertiesAdapter = new MyPropertiesAdapter(propertiesList, this);

        propertiesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        propertiesRecyclerView.setAdapter(propertiesAdapter);

        swipeRefreshLayout.setColorSchemeResources(R.color.main_color);
        swipeRefreshLayout.setOnRefreshListener(this::loadUserProperties);
    }

    private void setupClickListeners() {
        addPropertyButton.setOnClickListener(v -> openAddPropertyActivity());
        emptyAddPropertyButton.setOnClickListener(v -> openAddPropertyActivity());
        retryButton.setOnClickListener(v -> loadUserProperties());

    }

    private void initializeData() {
        apiService = RetrofitClient.getApiService();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void loadUserProperties() {
        if (currentUser == null) {
            showError("User not authenticated. Please login again.");
            return;
        }

        showLoadingState();

        Call<ResponseDTO> call = apiService.getUserProperties(currentUser.getUid());
        call.enqueue(new Callback<ResponseDTO>() {
            @Override
            public void onResponse(Call<ResponseDTO> call, Response<ResponseDTO> response) {
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    ResponseDTO propertyResponse = response.body();
                    List<Property> properties = propertyResponse.getMessageBody();

                    if (properties != null && !properties.isEmpty()) {
                        updatePropertiesList(properties);
                        showPropertiesState();
                    } else {
                        showEmptyState(); // Show "Property not found" when list is empty
                    }
                } else {
                    String error = "Failed to load properties";
                    if (response.code() == 404) {
                        showEmptyState(); // Show "Property not found" for 404 response
                    } else {
                        try {
                            if (response.errorBody() != null) {
                                error = response.errorBody().string();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body: " + e.getMessage());
                        }
                        showError(error);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseDTO> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Log.e(TAG, "Network error: " + t.getMessage(), t);
                showError("Network error. Please check your connection and try again.");
            }
        });
    }
    private void updatePropertiesList(List<Property> properties) {
        propertiesList.clear();
        propertiesList.addAll(properties);
        propertiesAdapter.notifyDataSetChanged();
    }

    private void deleteProperty(Property property, int position) {
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Delete Property")
                .setMessage("Are you sure you want to delete this property? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> performDelete(property, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performDelete(Property property, int position) {
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<ResponseDTO> call = apiService.deleteProperty(property.getId(), currentUser.getUid());
        call.enqueue(new Callback<ResponseDTO>() {
            @Override
            public void onResponse(Call<ResponseDTO> call, Response<ResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ResponseDTO responseDTO = response.body();

                    if (responseDTO.getStatusCode() == 0) {
                        // Remove the property from the local list
                        propertiesList.remove(position);
                        // Notify the adapter (fallback to notifyDataSetChanged)
                        propertiesAdapter.notifyDataSetChanged();

                        // Update UI state based on list size
                        if (propertiesList.isEmpty()) {
                            showEmptyState();
                        } else {
                            showPropertiesState();
                        }

                        Toast.makeText(getContext(), "Property deleted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), responseDTO.getStatusMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to delete property", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseDTO> call, Throwable t) {
                Log.e(TAG, "Delete error: " + t.getMessage());
                Toast.makeText(getContext(), "Network error while deleting", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void openAddPropertyActivity() {
        Intent intent = new Intent(getContext(), PostPropertyActivity.class);
        startActivity(intent);
    }

    private void openPropertyDetails(Property property) {
        Intent intent = new Intent(getContext(), PropertyDetailsActivity.class);
        intent.putExtra("PROPERTY", property);
        startActivity(intent);
    }


    private void openEditProperty(Property property) {
        Intent intent = new Intent(getContext(), PostPropertyActivity.class);
        intent.putExtra("PROPERTY_TO_EDIT", property);
        intent.putExtra("IS_EDIT_MODE", true);
        startActivityForResult(intent, EDIT_PROPERTY_REQUEST_CODE);
    }

    // State management methods
    private void showLoadingState() {
        loadingLayout.setVisibility(View.VISIBLE);
        propertiesRecyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);
        errorStateLayout.setVisibility(View.GONE);
    }

    private void showPropertiesState() {
        loadingLayout.setVisibility(View.GONE);
        propertiesRecyclerView.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
        errorStateLayout.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        loadingLayout.setVisibility(View.GONE);
        propertiesRecyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
        errorStateLayout.setVisibility(View.GONE);
    }

    private void showError(String error) {
        loadingLayout.setVisibility(View.GONE);
        propertiesRecyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);
        errorStateLayout.setVisibility(View.VISIBLE);
        errorMessage.setText(error);
    }

    // MyPropertiesAdapter.OnPropertyActionListener implementation
    @Override
    public void onViewClicked(Property property) {
        openPropertyDetails(property);
    }

    @Override
    public void onEditClicked(Property property) {
        openEditProperty(property);
    }

    @Override
    public void onDeleteClicked(Property property, int position) {
        deleteProperty(property, position);
    }
    // Add this method to handle the result from edit activity
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_PROPERTY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Refresh the properties list
            loadUserProperties();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        loadUserProperties();
    }
}