package com.example.citylinkrentals.Activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.citylinkrentals.Adapter.PropertyAdapter;
import com.example.citylinkrentals.R;
import com.example.citylinkrentals.model.Property;
import com.example.citylinkrentals.model.ResponseDTO;
import com.example.citylinkrentals.network.ApiService;
import com.example.citylinkrentals.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchPropertyActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PropertyAdapter propertyAdapter;
    private List<Property> propertyList = new ArrayList<>();
    private EditText searchBar;
    MaterialButton back_icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_property);

        recyclerView = findViewById(R.id.listView);
        searchBar = findViewById(R.id.search_bar);
        back_icon = findViewById(R.id.back_arrow);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        propertyAdapter = new PropertyAdapter(this, propertyList);
        recyclerView.setAdapter(propertyAdapter);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fetchProperties();

        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = searchBar.getText().toString().trim();
                filterProperties(query);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void fetchProperties() {
        ApiService apiService = RetrofitClient.getApiService();
        Call<ResponseDTO> call = apiService.getAllProperties();
        call.enqueue(new Callback<ResponseDTO>() {
            @Override
            public void onResponse(Call<ResponseDTO> call, Response<ResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    propertyList.clear();
                    propertyList.addAll(response.body().getMessageBody());
                    propertyAdapter.updateList(propertyList);
                } else {
                    Toast.makeText(SearchPropertyActivity.this, "Failed to load properties", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseDTO> call, Throwable t) {
                Toast.makeText(SearchPropertyActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterProperties(String query) {
        List<Property> filteredList = new ArrayList<>();
        for (Property property : propertyList) {
            if (property.getCity() != null && property.getCity().toLowerCase().contains(query.toLowerCase()) ||
                    property.getLocality() != null && property.getLocality().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(property);
            }
        }
        propertyAdapter.updateList(filteredList);
    }
}