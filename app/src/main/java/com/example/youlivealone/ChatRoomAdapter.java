package com.example.youlivealone;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {

    private ArrayList<ChatRoom> chatRooms;
    private Context context;

    public ChatRoomAdapter(ArrayList<ChatRoom> chatRooms, Context context) {
        this.chatRooms = chatRooms;
        this.context = context;
    }

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_room, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
        ChatRoom chatRoom = chatRooms.get(position);

        // 각 채팅방의 제목, 참여자 수, 마지막 메시지 시간을 설정
        holder.chatRoomTitle.setText(chatRoom.getName());
        holder.participantCount.setText("참여자 수: " + chatRoom.getParticipantCount());

        // 애초에 지금 마지막 시간이나 마지막 채팅을 DB에 저장하거나 가져오는 로직이 없음.
        // 마지막 메시지 시간 표시 설정
        if (chatRoom.getLastMessageTime() != null) {
            holder.lastMessageTime.setText(chatRoom.getLastMessageTime());
            Log.d("ChatRoomAdapter", "Last message time: " + chatRoom.getLastMessageTime());
        } else {
            holder.lastMessageTime.setText("");
            Log.d("ChatRoomAdapter", "No recent messages");
        }

        // 채팅방 클릭 시 ChatRoomActivity로 이동하며 ID와 이름을 Intent에 전달
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatRoomActivity.class);
            intent.putExtra("chatRoomId", chatRoom.getId());
            intent.putExtra("chatRoomName", chatRoom.getName()); // 채팅방 이름도 전달
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    public static class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        TextView chatRoomTitle, participantCount, lastMessageTime;

        public ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            chatRoomTitle = itemView.findViewById(R.id.chatroom_name);
            participantCount = itemView.findViewById(R.id.chatroom_participants);
            lastMessageTime = itemView.findViewById(R.id.chatroom_last_message_time);
        }
    }
}
