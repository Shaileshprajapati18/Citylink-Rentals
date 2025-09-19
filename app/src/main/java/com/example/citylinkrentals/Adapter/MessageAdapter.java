package com.example.citylinkrentals.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.citylinkrentals.R;
import com.example.citylinkrentals.model.MessageDTO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1; // User sent message
    private static final int VIEW_TYPE_RECEIVED = 2; // AI/System received message

    private List<MessageDTO> messages;
    private Context context;

    public MessageAdapter(List<MessageDTO> messages, Context context) {
        this.messages = messages;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        MessageDTO message = messages.get(position);
        return message.getIsSent() ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == VIEW_TYPE_SENT) {
            View view = inflater.inflate(R.layout.chat_send, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.chat_receive, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageDTO message = messages.get(position);

        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ViewHolder for sent messages (user messages)
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        TextView tvTimestamp;

        SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }

        void bind(MessageDTO message) {
            tvMessage.setText(message.getContent());

            if (tvTimestamp != null) {
                String formattedTime = formatTimestamp(message.getTimestamp());
                tvTimestamp.setText(formattedTime);
            }
        }
    }

    // ViewHolder for received messages (AI/system messages)
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        TextView tvTimestamp;

        ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }

        void bind(MessageDTO message) {
            tvMessage.setText(message.getContent());

            if (tvTimestamp != null) {
                String formattedTime = formatTimestamp(message.getTimestamp());
                tvTimestamp.setText(formattedTime);
            }
        }
    }

    // Helper method to format timestamp
    private static String formatTimestamp(String timestamp) {
        if (timestamp == null) return "";

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(timestamp);
                    return dateTime.format(DateTimeFormatter.ofPattern("hh:mm a"));
                } catch (Exception e) {
                }
            }

            String[] formats = {
                    "yyyy-MM-dd'T'HH:mm:ss",
                    "yyyy-MM-dd HH:mm:ss",
                    "dd/MM/yyyy HH:mm:ss",
                    "MM/dd/yyyy HH:mm:ss"
            };

            for (String format : formats) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                    Date date = sdf.parse(timestamp);
                    SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm a", Locale.getDefault());
                    return outputFormat.format(date);
                } catch (ParseException e) {
                }
            }

            if (timestamp.length() >= 5) {
                return timestamp.substring(0, 5);
            }

        } catch (Exception e) {
        }
        return "";
    }
}