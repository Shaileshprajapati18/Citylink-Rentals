package com.example.citylinkrentals.model;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.citylinkrentals.model.Property;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FavoriteManager {
    private static final String PREF_NAME = "favorite_properties";
    private static final String KEY_FAVORITES = "favorites_list";
    private SharedPreferences preferences;
    private Gson gson;
    private static FavoriteManager instance;

    private FavoriteManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static synchronized FavoriteManager getInstance(Context context) {
        if (instance == null) {
            instance = new FavoriteManager(context.getApplicationContext());
        }
        return instance;
    }

    // Add property to favorites
    public boolean addToFavorites(Property property) {
        if (property == null || property.getId() == null) return false;
        
        List<Property> favorites = getFavorites();
        
        // Check if already exists
        for (Property fav : favorites) {
            if (fav.getId().equals(property.getId())) {
                return false; // Already exists
            }
        }
        
        favorites.add(property);
        return saveFavorites(favorites);
    }

    // Remove property from favorites
    public boolean removeFromFavorites(Property property) {
        if (property == null || property.getId() == null) return false;
        
        List<Property> favorites = getFavorites();
        boolean removed = favorites.removeIf(fav -> fav.getId().equals(property.getId()));
        
        if (removed) {
            return saveFavorites(favorites);
        }
        return false;
    }

    // Remove by ID
    public boolean removeFromFavorites(Long propertyId) {
        if (propertyId == null) return false;
        
        List<Property> favorites = getFavorites();
        boolean removed = favorites.removeIf(fav -> fav.getId().equals(propertyId));
        
        if (removed) {
            return saveFavorites(favorites);
        }
        return false;
    }

    // Check if property is favorite
    public boolean isFavorite(Property property) {
        if (property == null || property.getId() == null) return false;
        
        List<Property> favorites = getFavorites();
        return favorites.stream().anyMatch(fav -> fav.getId().equals(property.getId()));
    }

    // Check if property is favorite by ID
    public boolean isFavorite(Long propertyId) {
        if (propertyId == null) return false;
        
        List<Property> favorites = getFavorites();
        return favorites.stream().anyMatch(fav -> fav.getId().equals(propertyId));
    }

    // Get all favorite properties
    public List<Property> getFavorites() {
        String json = preferences.getString(KEY_FAVORITES, "");
        if (json.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            Type type = new TypeToken<List<Property>>() {}.getType();
            List<Property> favorites = gson.fromJson(json, type);
            return favorites != null ? favorites : new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Get favorites count
    public int getFavoritesCount() {
        return getFavorites().size();
    }

    // Save favorites to SharedPreferences
    private boolean saveFavorites(List<Property> favorites) {
        try {
            String json = gson.toJson(favorites);
            return preferences.edit().putString(KEY_FAVORITES, json).commit();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Clear all favorites
    public boolean clearAllFavorites() {
        return preferences.edit().remove(KEY_FAVORITES).commit();
    }

    // Toggle favorite status
    public boolean toggleFavorite(Property property) {
        if (isFavorite(property)) {
            return removeFromFavorites(property);
        } else {
            return addToFavorites(property);
        }
    }
}
