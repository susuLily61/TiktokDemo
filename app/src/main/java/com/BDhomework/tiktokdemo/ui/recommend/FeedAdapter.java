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
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;


public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    public interface OnFeedClickListener {
        void onFeedClick(View sharedView, int position, FeedItem item);
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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feed_card, parent, false);
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
        private final TextView date;
        private final TextView likeCount;
        private final ShapeableImageView avatar;
        private final ImageView likeButton;

        private boolean liked = false;
        private int baseLikeCount = 0;

        FeedViewHolder(@NonNull View itemView) {
            super(itemView);

            cover = itemView.findViewById(R.id.feed_cover);
            title = itemView.findViewById(R.id.feed_title);
            author = itemView.findViewById(R.id.feed_author);
            date = itemView.findViewById(R.id.feed_date);
            likeCount = itemView.findViewById(R.id.feed_likes);
            avatar = itemView.findViewById(R.id.feed_avatar);
            likeButton = itemView.findViewById(R.id.feed_like_button);
        }

        void bind(FeedItem item) {

            Context context = itemView.getContext();

            title.setText(item.getTitle());
            author.setText(item.getAuthorName());
            date.setText(item.getDate());

            baseLikeCount = item.getLikeCount();
            liked = false;
            likeCount.setText(String.valueOf(baseLikeCount));
            likeButton.setImageResource(R.drawable.ic_heart_outline_black);

            // 计算当前每一列的宽度
            int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            final int columnWidth = screenWidth / 2;   // 你的 StaggeredGridLayoutManager 是 2 列

            Glide.with(context)
                    .asBitmap()
                    .load(item.getCoverUrl())
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource,
                                                    @Nullable Transition<? super Bitmap> transition) {

                            int w = resource.getWidth();
                            int h = resource.getHeight();

                            // 横图矮一点，竖图高一点
                            float ratio = (w > h) ? (3f / 4f) : (4f / 3f);

                            int newHeight = (int) (columnWidth * ratio);

                            ViewGroup.LayoutParams params = cover.getLayoutParams();
                            params.width = columnWidth;
                            params.height = newHeight;
                            cover.setLayoutParams(params);

                            cover.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            cover.setImageBitmap(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });

            // 头像
            Glide.with(context)
                    .load(item.getAvatarUrl())
                    .circleCrop()
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .into(avatar);

            // 跳转
            itemView.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onFeedClick(cover, pos, item);
                }
            });

            likeButton.setOnClickListener(v -> toggleLike());
        }

        private void toggleLike() {
            liked = !liked;

            int displayCount = liked ? baseLikeCount + 1 : baseLikeCount;
            likeCount.setText(String.valueOf(displayCount));

            likeButton.setImageResource(
                    liked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline_black
            );
        }
    }
}
