package com.BDhomework.tiktokdemo.comment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.BDhomework.tiktokdemo.R;
import com.BDhomework.tiktokdemo.model.Comment;

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final List<Comment> data = new ArrayList<>();

    public void submitList(List<Comment> comments) {
        data.clear();
        data.addAll(comments);
        notifyDataSetChanged();
    }

    public void prependComment(Comment comment) {
        data.add(0, comment);
        notifyItemInserted(0);
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {

        private final TextView user;
        private final TextView content;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            user = itemView.findViewById(R.id.comment_user);
            content = itemView.findViewById(R.id.comment_content);
        }

        void bind(Comment comment) {
            user.setText(comment.getUser());
            content.setText(comment.getContent());
        }
    }
}
