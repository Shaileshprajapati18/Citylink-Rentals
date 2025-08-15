package com.example.citylinkrentals.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.example.citylinkrentals.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeFragment extends Fragment {

    private BottomNavigationView bottomNavigationView;
    private NestedScrollView main;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
        main = view.findViewById(R.id.main);

        main.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)
                (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    if (scrollY > oldScrollY) {
                        hideBottomNav(); // scrolling down
                    } else if (scrollY < oldScrollY) {
                        showBottomNav(); // scrolling up
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
