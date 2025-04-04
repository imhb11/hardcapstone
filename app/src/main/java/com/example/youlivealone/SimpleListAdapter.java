package com.example.youlivealone;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SimpleListAdapter extends RecyclerView.Adapter<SimpleListAdapter.ViewHolder> {

    private List<String> dataList;
    private String viewType;

    public SimpleListAdapter(List<String> dataList, String viewType) {
        this.dataList = dataList;
        this.viewType = viewType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_simple_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String itemText = dataList.get(position);
        holder.textViewItem.setText(itemText);

        // 예: 아이템 클릭 이벤트 처리
        holder.itemView.setOnClickListener(v -> {
            // 각 타입에 따른 처리
            if ("post".equals(viewType)) {
                // 게시글 클릭 이벤트 처리
            } else if ("comment".equals(viewType)) {
                // 댓글 클릭 이벤트 처리
            } else if ("liked post".equals(viewType)) {
                // 공감한 게시글
            } else if ("chat".equals(viewType)){
                // 채팅방
            } // ... 기타 처리
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewItem;

        ViewHolder(View itemView) {
            super(itemView);
            textViewItem = itemView.findViewById(R.id.text_view_item);
        }
    }
}
