package com.example.youlivealone;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MeetingCategoryListAdapter extends RecyclerView.Adapter<MeetingCategoryListAdapter.ViewHolder>{
    private final List<Meeting_post> posts;
    private final Context context;

    public MeetingCategoryListAdapter(Context context, List<Meeting_post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Meeting_post post = posts.get(position);
        holder.titleText.setText(post.getTitle());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, Meeting_detail.class);
            intent.putExtra("postId", post.getId());
            intent.putExtra("title", post.getTitle());
            intent.putExtra("introduction", post.getIntroduction());
            intent.putExtra("content", post.getContent());
            intent.putExtra("memberCount", post.getMemberCount());
            intent.putExtra("latitude", post.getLatitude());
            intent.putExtra("longitude", post.getLongitude());
            intent.putExtra("meetingCategoryId", post.getMeetingCategoryId());
            intent.putExtra("subcategoryId", post.getSubcategoryID());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(android.R.id.text1);
        }
    }
}
