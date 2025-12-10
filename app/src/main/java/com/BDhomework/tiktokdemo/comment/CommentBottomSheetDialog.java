package com.BDhomework.tiktokdemo.comment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.BDhomework.tiktokdemo.R;
import com.BDhomework.tiktokdemo.data.MockFeedRepository;
import com.BDhomework.tiktokdemo.model.Comment;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class CommentBottomSheetDialog extends BottomSheetDialogFragment {

    public interface OnCommentAddedListener {
        void onCommentAdded(int totalCount);
    }

    private OnCommentAddedListener onCommentAddedListener;

    public void setOnCommentAddedListener(OnCommentAddedListener listener) {
        this.onCommentAddedListener = listener;
    }

    private final List<Comment> commentList = new ArrayList<>();
    private LinearLayout containerLayout;
    private EditText inputView;
    private TextView sendView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_comment_sheet, container, false);

        containerLayout = root.findViewById(R.id.comment_container);
        inputView = root.findViewById(R.id.comment_input);
        sendView = root.findViewById(R.id.comment_send);

        // 初始化 mock 评论
        commentList.clear();
        commentList.addAll(new MockFeedRepository().mockComments());
        for (Comment comment : commentList) {
            addCommentView(inflater, comment);
        }

        // 发送按钮
        sendView.setOnClickListener(v -> {
            String text = inputView.getText().toString().trim();
            if (text.isEmpty()) return;

            Comment newComment = new Comment("我", text);
            commentList.add(0, newComment); // 插在最前面
            addCommentView(inflater, newComment /* 可选加到最上面 */);

            inputView.setText("");

            if (onCommentAddedListener != null) {
                onCommentAddedListener.onCommentAdded(commentList.size());
            }
        });

        return root;
    }

    private void addCommentView(LayoutInflater inflater, Comment comment) {
        View item = inflater.inflate(R.layout.item_comment, containerLayout, false);
        TextView user = item.findViewById(R.id.comment_user);
        TextView content = item.findViewById(R.id.comment_content);
        user.setText(comment.getUser());
        content.setText(comment.getContent());
        containerLayout.addView(item, 0); // 加到容器顶部
    }
}
