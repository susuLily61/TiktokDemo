package com.BDhomework.tiktokdemo.ui.recommend;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.BDhomework.tiktokdemo.R;
import com.BDhomework.tiktokdemo.model.FeedItem;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    public interface OnFeedClickListener {
        void onFeedClick(int position, FeedItem item);
    }

    private final List<FeedItem> data = new ArrayList<>();
    private final OnFeedClickListener listener;

    public FeedAdapter(OnFeedClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<FeedItem> list) {
        data.clear();
        data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feed_card, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class FeedViewHolder extends RecyclerView.ViewHolder {
        private final ImageView cover;
        private final TextView title;
        private final TextView author;
        private final TextView likes;
        private final ShapeableImageView avatar;

        FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.feed_cover);
            title = itemView.findViewById(R.id.feed_title);
            author = itemView.findViewById(R.id.feed_author);
            likes = itemView.findViewById(R.id.feed_likes);
            avatar = itemView.findViewById(R.id.feed_avatar);
        }

        void bind(FeedItem item) {
            Context context = itemView.getContext();
            title.setText(item.getTitle());
            author.setText(item.getAuthorName());
            likes.setText(String.valueOf(item.getLikeCount()));

            Glide.with(context)
                    .load(item.getCoverUrl())
                    .centerCrop()
                    .placeholder(R.drawable.ic_placeholder)
                    .into(cover);

            Glide.with(context)
                    .load(item.getAvatarUrl())
                    .circleCrop()
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .into(avatar);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onFeedClick(position, item);
                    }
                }
            });
        }
    }
}
