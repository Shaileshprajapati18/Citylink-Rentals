package com.example.citylinkrentals.network;

import com.example.citylinkrentals.model.MessageDTO;
import com.example.citylinkrentals.model.PropertyListResponse;
import com.example.citylinkrentals.model.ResponseDTO;
import com.example.citylinkrentals.model.User;
import com.example.citylinkrentals.model.UserProfileResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
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

    @POST("api/users")
    Call<UserProfileResponse> updateUser(@Body User user);

    @GET("api/users")
    Call<UserProfileResponse> getUserProfile(@Query("firebaseUid") String firebaseUid);

    @GET("/api/properties/by-user")
    Call<ResponseDTO> getUserProperties(
            @Query("firebaseUid") String firebaseUid
    );

    @GET("/api/properties/by-user")
    Call<ResponseDTO> deleteProperty(
            @Query("firebaseUid") Long firebaseUid
    );

    @GET("/api/properties/by-city-category")
    Call<ResponseDTO> getAllProperties();

    @POST("api/chat")
    Call<MessageDTO> sendMessage(@Query("userUid") String userUid, @Body String messageContent);

    @GET("api/chat")
    Call<List<MessageDTO>> getMessages(@Query("userUid") String userUid);

    @POST("api/chat/reply")
    Call<MessageDTO> sendAdminReply(@Query("userUid") String userUid, @Body String replyContent);

    @DELETE("api/properties/{id}")
    Call<ResponseDTO> deleteProperty(
            @Path("id") Long propertyId,
            @Query("firebaseUid") String firebaseUid
    );

    @GET("api/properties/{id}")
    Call<ResponseDTO> getPropertyById(@Path("id") Long propertyId);

    @PUT("api/properties/{id}")
    @Multipart
    Call<ResponseDTO> updateProperty(
            @Path("id") Long propertyId,
            @Part("property") RequestBody property,
            @Part List<MultipartBody.Part> images
    );
}
