package com.example.youlivealone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class PostListAdapter extends ArrayAdapter<Post> {
    private Context context;
    private List<Post> posts;

    public PostListAdapter(Context context, List<Post> posts) {
        super(context, 0, posts);
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Post post = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.community_postlist, parent, false);
        }

        TextView titleTextView = convertView.findViewById(R.id.postTitle);
        TextView heartCountTextView = convertView.findViewById(R.id.likeCount);
        // ImageView heartIcon = convertView.findViewById(R.id.heart_icon); // 필요시 클릭 이벤트 가능

        titleTextView.setText(post.getTitle());
        heartCountTextView.setText(String.valueOf(post.getLikes())); // Post에 likes 필드 있어야 함

        return convertView;
    }
}
