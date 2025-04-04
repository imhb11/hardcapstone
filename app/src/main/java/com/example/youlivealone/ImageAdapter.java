package com.example.youlivealone;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;
import com.example.youlivealone.R;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private List<Integer> images;
    private ViewPager2 mviewPager2;

    public ImageAdapter(List<Integer> images, ViewPager2 viewPager2) {
        this.images = new ArrayList<>(images);
        this.mviewPager2 = viewPager2;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        holder.imageView.setImageResource(images.get(position));
        if (position == images.size() - 2) {
            mviewPager2.post(runnable);
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            images.addAll(images);
            notifyDataSetChanged();
        }
    };
}
