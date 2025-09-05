package com.example.citylinkrentals.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.citylinkrentals.R;
import com.example.citylinkrentals.model.MessageDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<MessageDTO> messages;
    private Context context;

    public MessageAdapter(List<MessageDTO> messages, Context context) {
        this.messages = messages;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isSent() ? 0 : 1;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) {
            view = LayoutInflater.from(context).inflate(R.layout.chat_send, parent, false);
        } else { // Received message
            view = LayoutInflater.from(context).inflate(R.layout.chat_receive, parent, false);
        }
        return new MessageViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageDTO message = messages.get(position);
        if (holder.tvMessage != null) {
            holder.tvMessage.setText(message.getContent());
        } else {
            Log.e("MessageAdapter", "tvMessage is null for position: " + position);
        }
        LocalDateTime dateTime = LocalDateTime.parse(message.getTimestamp());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        String formattedTime = dateTime.format(formatter);
        holder.tvTime.setText(formattedTime);

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;

        public MessageViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            if (viewType == 0) { // Sent
                tvMessage = itemView.findViewById(R.id.tvMessageSent);
                tvTime = itemView.findViewById(R.id.tvTimeSent);
            } else { // Received
                tvMessage = itemView.findViewById(R.id.tvMessageReceived);
                tvTime = itemView.findViewById(R.id.tvTimeReceived);
            }
            if (tvMessage == null || tvTime == null) {
                Log.e("MessageAdapter", "View initialization failed for viewType: " + viewType);
            }
        }
    }
}