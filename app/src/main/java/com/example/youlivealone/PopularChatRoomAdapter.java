package com.example.youlivealone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.youlivealone.ChatRoom;

import java.util.List;

public class PopularChatRoomAdapter extends RecyclerView.Adapter<PopularChatRoomAdapter.ViewHolder> {
    private List<ChatRoom> popularChatRooms;
    private Context context;

    public PopularChatRoomAdapter(List<ChatRoom> popularChatRooms, Context context) {
        this.popularChatRooms = popularChatRooms;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_popular_chat_room, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatRoom chatRoom = popularChatRooms.get(position);
        holder.chatRoomTitle.setText(chatRoom.getName());
        holder.participantCount.setText("참여자 수: " + chatRoom.getParticipantCount());
    }

    @Override
    public int getItemCount() {
        return popularChatRooms.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView chatRoomTitle, participantCount;

        ViewHolder(View itemView) {
            super(itemView);
            chatRoomTitle = itemView.findViewById(R.id.chatroom_name);
            participantCount = itemView.findViewById(R.id.chatroom_participants);
        }
    }
}
