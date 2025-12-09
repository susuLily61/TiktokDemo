package com.BDhomework.tiktokdemo.comment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.BDhomework.tiktokdemo.R;
import com.BDhomework.tiktokdemo.data.MockFeedRepository;
import com.BDhomework.tiktokdemo.model.Comment;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class CommentBottomSheetDialog extends BottomSheetDialogFragment {

    /**
     * 评论数变化回调
     */
    public interface OnCommentAddedListener {
        void onAdded(int totalCount);
    }

    private OnCommentAddedListener listener;

    public void setOnCommentAddedListener(OnCommentAddedListener listener) {
        this.listener = listener;
    }

    private final List<Comment> comments = new ArrayList<>();
    private CommentAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_comment_sheet, container, false);

        RecyclerView commentList = root.findViewById(R.id.comment_list);
        EditText input = root.findViewById(R.id.comment_input);
        TextView send = root.findViewById(R.id.comment_send);

        // 初始化评论数据（先用 Mock）
        comments.clear();
        comments.addAll(new MockFeedRepository().mockComments());

        // RecyclerView 设置
        commentList.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CommentAdapter(comments);
        commentList.setAdapter(adapter);

        // 发送评论按钮（先实现一个简单版本，之后你可以再改）
        send.setOnClickListener(v -> {
            String text = input.getText().toString().trim();
            if (TextUtils.isEmpty(text)) {
                return;
            }
            // 这里先用一个固定用户昵称，之后可以从登录信息里取
            Comment newComment = new Comment("我", text);
            comments.add(newComment);
            adapter.notifyItemInserted(comments.size() - 1);
            commentList.scrollToPosition(comments.size() - 1);
            input.setText("");

            // 回调外部，更新评论数
            if (listener != null) {
                listener.onAdded(comments.size());
            }
        });

        // 打开时也可以把当前总数回调一次（可选）
        if (listener != null) {
            listener.onAdded(comments.size());
        }

        return root;
    }

    /**
     * 简单的评论列表 Adapter
     */
    private static class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

        private final List<Comment> comments;

        CommentAdapter(List<Comment> comments) {
            this.comments = comments;
        }

        @NonNull
        @Override
        public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_comment, parent, false);
            return new CommentViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
            Comment comment = comments.get(position);
            holder.user.setText(comment.getUser());
            holder.content.setText(comment.getContent());
        }

        @Override
        public int getItemCount() {
            return comments == null ? 0 : comments.size();
        }

        static class CommentViewHolder extends RecyclerView.ViewHolder {

            TextView user;
            TextView content;

            CommentViewHolder(@NonNull View itemView) {
                super(itemView);
                user = itemView.findViewById(R.id.comment_user);
                content = itemView.findViewById(R.id.comment_content);
            }
        }
    }
}
