package com.example.citylinkrentals.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.citylinkrentals.Adapter.MessageAdapter;
import com.example.citylinkrentals.R;
import com.example.citylinkrentals.model.MessageDTO;
import com.example.citylinkrentals.network.ApiService;
import com.example.citylinkrentals.network.RetrofitClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AiChatFragment extends Fragment {

    private RecyclerView rvMessages;
    private EditText etMessageInput;
    private ImageView btnSend;
    private MessageAdapter messageAdapter;
    private List<MessageDTO> messageList = new ArrayList<>();
    private String userUid;
    private Handler mainHandler;
    private Executor executor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai_chat, container, false);

        rvMessages = view.findViewById(R.id.rvMessages);
        etMessageInput = view.findViewById(R.id.etMessageInput);
        btnSend = view.findViewById(R.id.btnSend);
        mainHandler = new Handler(Looper.getMainLooper());
        executor = Executors.newSingleThreadExecutor();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true); // Start from bottom
        rvMessages.setLayoutManager(layoutManager);
        messageAdapter = new MessageAdapter(messageList, getContext());
        rvMessages.setAdapter(messageAdapter);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            userUid = firebaseUser.getUid();
        } else {
            mainHandler.post(() ->
                    Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show());
            return view;
        }

        btnSend.setOnClickListener(v -> sendMessage());

        fetchMessages();

        return view;
    }

    private void sendMessage() {
        String messageText = etMessageInput.getText().toString().trim();
        if (messageText.isEmpty()) {
            mainHandler.post(() ->
                    Toast.makeText(getContext(), "Please enter a message", Toast.LENGTH_SHORT).show());
            return;
        }

        MessageDTO sentMessage = new MessageDTO(messageText, true, LocalDateTime.now().toString());
        messageList.add(sentMessage);
        mainHandler.post(() -> {
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            rvMessages.smoothScrollToPosition(messageList.size() - 1);
            etMessageInput.setText("");
            Toast.makeText(getContext(), "Message sent", Toast.LENGTH_SHORT).show();
        });

        ApiService apiService = RetrofitClient.getApiService();
        Call<MessageDTO> call = apiService.sendMessage(userUid, messageText);
        executor.execute(() -> call.enqueue(new Callback<MessageDTO>() {
            @Override
            public void onResponse(Call<MessageDTO> call, Response<MessageDTO> response) {
                Log.d("AiChatFragment", "onResponse: " + (response.isSuccessful() ? "Success" : "Failure") + ", Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("AiChatFragment", "Server response ignored: " + response.body().getContent());
                } else {
                    mainHandler.post(() ->
                            Toast.makeText(getContext(), "Server error, response not added", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onFailure(Call<MessageDTO> call, Throwable t) {
                Log.e("AiChatFragment", "onFailure: " + t.getMessage());
                mainHandler.post(() ->
                        Toast.makeText(getContext(), "Error sending message: " + t.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }));
    }

    private void fetchMessages() {
        ApiService apiService = RetrofitClient.getApiService();
        Call<List<MessageDTO>> call = apiService.getMessages(userUid);
        executor.execute(() -> call.enqueue(new Callback<List<MessageDTO>>() {
            @Override
            public void onResponse(Call<List<MessageDTO>> call, Response<List<MessageDTO>> response) {
                Log.d("AiChatFragment", "fetchMessages onResponse: " + (response.isSuccessful() ? "Success" : "Failure") + ", Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    List<MessageDTO> fetchedMessages = response.body();
                    mainHandler.post(() -> {
                        messageList.clear();
                        messageList.addAll(fetchedMessages);
                        Log.d("AiChatFragment", "Messages fetched: " + messageList.size());
                        messageAdapter.notifyDataSetChanged();
                        if (!messageList.isEmpty()) {
                            rvMessages.smoothScrollToPosition(messageList.size() - 1);
                        }
                        Toast.makeText(getContext(), "Chat loaded successfully", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    mainHandler.post(() ->
                            Toast.makeText(getContext(), "Failed to fetch messages", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onFailure(Call<List<MessageDTO>> call, Throwable t) {
                Log.e("AiChatFragment", "fetchMessages onFailure: " + t.getMessage());
                mainHandler.post(() ->
                        Toast.makeText(getContext(), "Error fetching messages: " + t.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }));
    }
}