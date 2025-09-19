package com.example.citylinkrentals.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.citylinkrentals.Adapter.MessageAdapter;
import com.example.citylinkrentals.R;
import com.example.citylinkrentals.model.MessageDTO;
import com.example.citylinkrentals.network.ApiService;
import com.example.citylinkrentals.network.RetrofitClient;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AiChatFragment extends Fragment {
    private static final String TAG = "AiChatFragment";
    private static final int POLLING_INTERVAL = 3000;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rvMessages;
    private EditText etMessageInput;
    private ImageView btnSend, fabRefresh;
    private LinearLayout bottomInputLayout, typingIndicatorLayout;
    private TextView tvContactName, tvStatus;
    private CircularProgressIndicator typingProgress;

    private MessageAdapter messageAdapter;
    private List<MessageDTO> messageList = new ArrayList<>();
    private LinearLayoutManager layoutManager;

    private String userUid;
    private Handler mainHandler;
    private Handler pollingHandler;
    private View rootView;
    private ApiService apiService;

    private ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener;
    private int initialBottomPadding = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_ai_chat, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews();
        initializeData();
        setupRecyclerView();
        setupInputHandling();
        setupKeyboardHandling();
        rootViewHandling();

       }

    private void rootViewHandling() {

        ViewCompat.setOnApplyWindowInsetsListener(rootView, new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                int imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;

                if (imeHeight > 0) {
                    v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), imeHeight);
                } else {
                    v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), 0);
                }

                return insets;
            }
        });
        if (userUid != null) {
            fetchMessages();
        } else {
            showWelcomeMessage();
        }

    }

    private void initializeViews() {
        rvMessages = rootView.findViewById(R.id.rvMessages);
        etMessageInput = rootView.findViewById(R.id.etMessageInput);
        btnSend = rootView.findViewById(R.id.btnSend);
        fabRefresh = rootView.findViewById(R.id.fabRefresh);
        bottomInputLayout = rootView.findViewById(R.id.bottomInputLayout);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        tvContactName = rootView.findViewById(R.id.tvContactName);
        tvStatus = rootView.findViewById(R.id.tvStatus);

        typingIndicatorLayout = rootView.findViewById(R.id.typingIndicator);
        typingProgress = rootView.findViewById(R.id.typingProgress);

        if (btnSend != null) {
            btnSend.setEnabled(false);
            btnSend.setAlpha(0.5f);
        }
    }

    private void initializeData() {
        mainHandler = new Handler(Looper.getMainLooper());
        pollingHandler = new Handler(Looper.getMainLooper());
        apiService = RetrofitClient.getApiService();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            userUid = firebaseUser.getUid();
        } else {
            Log.w(TAG, "User not logged in");
        }
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeResources(R.color.main_color);
            swipeRefreshLayout.setOnRefreshListener(this::refreshMessages);
        }
        if (rvMessages != null) {
            initialBottomPadding = rvMessages.getPaddingBottom();
        }
    }

    // In refreshMessages() method
    private void refreshMessages() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }
        fetchMessages();
    }

    private void setupRecyclerView() {
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(false);
        rvMessages.setLayoutManager(layoutManager);
        messageAdapter = new MessageAdapter(messageList, getContext());
        rvMessages.setAdapter(messageAdapter);

        rvMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (layoutManager != null) {
                    int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
                    if (firstVisiblePosition > 0) {
                        if (fabRefresh != null && fabRefresh.getVisibility() != View.VISIBLE) {
                            fabRefresh.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (fabRefresh != null && fabRefresh.getVisibility() == View.VISIBLE) {
                            fabRefresh.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });
        messageAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                scrollToBottom();
            }
        });
    }

    private void setupInputHandling() {
        etMessageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = s.toString().trim().length() > 0;
                if (btnSend != null) {
                    btnSend.setEnabled(hasText);
                    btnSend.setAlpha(hasText ? 1.0f : 0.5f);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        if (btnSend != null) {
            btnSend.setOnClickListener(v -> sendMessage());
        }

        fabRefresh = rootView.findViewById(R.id.fabRefresh);
        if (fabRefresh != null) {
            fabRefresh.setOnClickListener(v -> refreshMessages());
        }

        etMessageInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });

        etMessageInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                mainHandler.postDelayed(this::scrollToBottom, 300);
            }
        });
    }

    private void setupKeyboardHandling() {
        globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            private int previousHeightDiff = 0;
            private boolean wasKeyboardOpen = false;

            @Override
            public void onGlobalLayout() {
                if (getActivity() == null || rootView == null) return;

                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootView.getRootView().getHeight();
                int heightDiff = screenHeight - r.bottom;

                boolean isKeyboardOpen = heightDiff > screenHeight * 0.15;

                if (isKeyboardOpen != wasKeyboardOpen) {
                    wasKeyboardOpen = isKeyboardOpen;
                    if (isKeyboardOpen) {
                        onKeyboardOpened(heightDiff);
                    } else {
                        onKeyboardClosed();
                    }
                }

                previousHeightDiff = heightDiff;
            }
        };

        if (rootView != null) {
            rootView.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
        }
    }

    private void onKeyboardOpened(int keyboardHeight) {
        if (rvMessages != null) {
            rvMessages.setPadding(
                    rvMessages.getPaddingLeft(),
                    rvMessages.getPaddingTop(),
                    rvMessages.getPaddingRight(),
                    initialBottomPadding + (keyboardHeight / 3)
            );
            mainHandler.postDelayed(this::scrollToBottom, 100);
        }
    }

    private void onKeyboardClosed() {
        if (rvMessages != null) {
            rvMessages.setPadding(
                    rvMessages.getPaddingLeft(),
                    rvMessages.getPaddingTop(),
                    rvMessages.getPaddingRight(),
                    initialBottomPadding
            );
        }
    }

    private void sendMessage() {
        String messageText = etMessageInput.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        MessageDTO userMessage = new MessageDTO();
        userMessage.setContent(messageText);
        userMessage.setIsSent(true);
        userMessage.setFirebaseUid(userUid);
        userMessage.setTimestamp(getCurrentTimestamp());
        addMessageToUI(userMessage);

        etMessageInput.setText("");

        showTypingIndicator();

        if (userUid != null) {
            sendMessageToServer(messageText);
        }
    }

    private void sendMessageToServer(String messageText) {
        Call<MessageDTO> call = apiService.sendMessage(userUid, messageText);
        call.enqueue(new Callback<MessageDTO>() {
            @Override
            public void onResponse(Call<MessageDTO> call, Response<MessageDTO> response) {
                mainHandler.post(() -> {
                    hideTypingIndicator();
                    if (response.isSuccessful() && response.body() != null) {
                        MessageDTO aiResponse = response.body();

                        if (!aiResponse.getIsSent()) {
                            if (aiResponse.getTimestamp() == null) {
                                aiResponse.setTimestamp(getCurrentTimestamp());
                            }
                            // Refresh the SwipeRefreshLayout to fetch the latest messages
                            refreshMessages();
                            Log.d(TAG, "AI Response received: " + aiResponse.getContent());
                        } else {
                            Log.w(TAG, "Received user message back instead of AI response, using fallback");
                            // Optionally refresh even in this case, if desired
                            refreshMessages();
                        }
                    } else {
                        Log.e(TAG, "Server response not successful: " + response.code());
                        showErrorMessage("Sorry, I couldn't process your message. Please try again.");

                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<MessageDTO> call, Throwable t) {
                Log.e(TAG, "Send message failed: " + t.getMessage());
                mainHandler.post(() -> {
                    hideTypingIndicator();
                    showErrorMessage("Network error. Please check your connection and try again.");

                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }
    private void addMessageToUI(MessageDTO message) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post(() -> addMessageToUI(message));
            return;
        }

        messageList.add(message);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
    }

    private void showErrorMessage(String error) {
        MessageDTO errorMessage = new MessageDTO();
        errorMessage.setContent(error);
        errorMessage.setIsSent(false);
        errorMessage.setTimestamp(getCurrentTimestamp());
        addMessageToUI(errorMessage);
    }

    private void fetchMessages() {
        Call<List<MessageDTO>> call1 = apiService.getMessages(userUid);
        call1.enqueue(new Callback<List<MessageDTO>>() {
            @Override
            public void onResponse(Call<List<MessageDTO>> call, Response<List<MessageDTO>> response) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                if (layoutManager != null && layoutManager.findFirstVisibleItemPosition() == 0) {
                    if (fabRefresh != null && fabRefresh.getVisibility() == View.VISIBLE) {
                        fabRefresh.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<MessageDTO>> call, Throwable t) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        Call<List<MessageDTO>> call = apiService.getMessages(userUid);
        call.enqueue(new Callback<List<MessageDTO>>() {
            @Override
            public void onResponse(Call<List<MessageDTO>> call, Response<List<MessageDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MessageDTO> fetchedMessages = response.body();
                    messageList.clear();

                    for (MessageDTO msg : fetchedMessages) {
                        if (msg.getTimestamp() == null || msg.getTimestamp().isEmpty()) {
                            msg.setTimestamp(getCurrentTimestamp());
                        }
                        messageList.add(msg);
                    }

                    messageAdapter.notifyDataSetChanged();

                    if (messageList.isEmpty()) {
                        showWelcomeMessage();
                    } else {
                        scrollToBottom();
                    }
                    Log.d(TAG, "Messages loaded: " + messageList.size());
                } else {
                    showWelcomeMessage();
                    Log.w(TAG, "Failed to fetch messages: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<MessageDTO>> call, Throwable t) {
                Log.e(TAG, "Fetch messages failed: " + t.getMessage());
                showWelcomeMessage();
            }
        });
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .format(new Date());
    }

    private void showWelcomeMessage() {
        MessageDTO welcomeMessage = new MessageDTO();
        welcomeMessage.setContent("Hello! Welcome to our support chat. Please describe your question or concern, and our support team will respond as soon as possible.\n\n" +
                "We can help you with:\n" +
                "• Property listings and searches\n" +
                "• Rental inquiries\n" +
                "• Account issues\n" +
                "• Technical support\n" +
                "• General questions\n\n" +
                "What can we help you with today?");
        welcomeMessage.setFirebaseUid(null);
        welcomeMessage.setIsSent(false);
        welcomeMessage.setTimestamp(getCurrentTimestamp());
        addMessageToUI(welcomeMessage);
    }

    private void showTypingIndicator() {
        if (typingIndicatorLayout != null) {
            typingIndicatorLayout.setVisibility(View.VISIBLE);
            scrollToBottom();
        }
    }

    private void hideTypingIndicator() {
        if (typingIndicatorLayout != null) {
            typingIndicatorLayout.setVisibility(View.GONE);
        }
    }

    private void scrollToBottom() {
        if (rvMessages != null && messageAdapter.getItemCount() > 0) {
            rvMessages.post(() -> rvMessages.smoothScrollToPosition(messageAdapter.getItemCount() - 1));
        }
    }

    private void startPollingForResponse() {
        stopPolling();
        pollingHandler.postDelayed(pollingRunnable, POLLING_INTERVAL);
    }

    private void stopPolling() {
        if (pollingHandler != null) {
            pollingHandler.removeCallbacks(pollingRunnable);
        }
    }

    private final Runnable pollingRunnable = new Runnable() {
        @Override
        public void run() {
            if (userUid != null) {
                Call<List<MessageDTO>> call = apiService.getMessages(userUid);
                call.enqueue(new Callback<List<MessageDTO>>() {
                    @Override
                    public void onResponse(Call<List<MessageDTO>> call, Response<List<MessageDTO>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<MessageDTO> newMessages = response.body();
                            if (newMessages.size() > messageList.size()) {
                                messageList.clear();
                                messageList.addAll(newMessages);
                                messageAdapter.notifyDataSetChanged();
                                scrollToBottom();
                            }
                        }
                        pollingHandler.postDelayed(pollingRunnable, POLLING_INTERVAL);
                    }

                    @Override
                    public void onFailure(Call<List<MessageDTO>> call, Throwable t) {
                        Log.e(TAG, "Polling failed: " + t.getMessage());
                        pollingHandler.postDelayed(pollingRunnable, POLLING_INTERVAL);
                    }
                });
            }
        }
    };
    // In AiChatFragment
    private BroadcastReceiver newMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            // Refresh messages when a new one arrives
            fetchMessages();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            LocalBroadcastManager.getInstance(requireContext())
                    .registerReceiver(newMessageReceiver, new IntentFilter("NEW_MESSAGE"));
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        if (tvStatus != null) {
            tvStatus.setText("Support Team • Available");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }

        if (userUid != null && !messageList.isEmpty()) {
            startPollingForResponse();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            LocalBroadcastManager.getInstance(requireContext())
                    .unregisterReceiver(newMessageReceiver);

            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }

        stopPolling();

        if (etMessageInput != null) {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etMessageInput.getWindowToken(), 0);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopPolling();

        if (rootView != null && globalLayoutListener != null) {
            rootView.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
        }

        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }

        if (pollingHandler != null) {
            pollingHandler.removeCallbacksAndMessages(null);
        }
    }
}