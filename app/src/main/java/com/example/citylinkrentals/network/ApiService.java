package com.example.citylinkrentals.network;

import android.util.Property;

import com.example.citylinkrentals.model.PropertyListResponse;
import com.example.citylinkrentals.model.ResponseDTO;
import com.example.citylinkrentals.model.User;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {
    @Multipart
    @POST("api/properties")
    Call<PropertyListResponse> createProperty(
            @Part("property") RequestBody property,
            @Part List<MultipartBody.Part> images
    );

    @POST("api/users")
    Call<User> postUser(@Body User user);

    @GET("/api/properties/by-city-category")
    Call<ResponseDTO> getAllProperties();

    @GET("api/properties/by-city-category")
    Call<ResponseDTO> getAllPropertiesByCityAndCategory
            (@Query("category") String category,
            @Query("city") String city);
}
