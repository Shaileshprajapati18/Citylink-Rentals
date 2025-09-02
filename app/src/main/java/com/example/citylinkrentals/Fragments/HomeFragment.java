package com.example.citylinkrentals.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.example.citylinkrentals.Activities.PostPropertyActivity;
import com.example.citylinkrentals.Activities.SearchPropertyActivity;
import com.example.citylinkrentals.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeFragment extends Fragment {

    private BottomNavigationView bottomNavigationView;
    Button btnPostProperty,btnSearchProperty;
    private NestedScrollView main;
    Spinner propertyType;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
        main = view.findViewById(R.id.main);
        btnPostProperty = view.findViewById(R.id.btnPostProperty);
        btnSearchProperty = view.findViewById(R.id.btnSearchProperty);

        propertyType = view.findViewById(R.id.propertyTypeSpinner);

        btnPostProperty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), PostPropertyActivity.class));
            }
        });
        btnSearchProperty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SearchPropertyActivity.class));
            }
        });
        main.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)
                (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    if (scrollY > oldScrollY) {
                        hideBottomNav();
                    } else if (scrollY < oldScrollY) {
                        showBottomNav();
                    }
                });

        return view;
    }

    private void hideBottomNav() {
        bottomNavigationView.animate()
                .translationY(bottomNavigationView.getHeight())
                .alpha(0f)
                .setDuration(200)
                .start();
    }

    private void showBottomNav() {
        bottomNavigationView.animate()
                .translationY(0)
                .alpha(1f)
                .setDuration(200)
                .start();
    }
}
