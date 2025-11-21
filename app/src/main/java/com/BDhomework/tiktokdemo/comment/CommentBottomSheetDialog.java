package com.BDhomework.tiktokdemo.comment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.BDhomework.tiktokdemo.R;
import com.BDhomework.tiktokdemo.data.MockFeedRepository;
import com.BDhomework.tiktokdemo.model.Comment;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public class CommentBottomSheetDialog extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_comment_sheet, container, false);
        LinearLayout containerLayout = root.findViewById(R.id.comment_container);

        List<Comment> comments = new MockFeedRepository().mockComments();
        for (Comment comment : comments) {
            View item = inflater.inflate(R.layout.item_comment, containerLayout, false);
            TextView user = item.findViewById(R.id.comment_user);
            TextView content = item.findViewById(R.id.comment_content);
            user.setText(comment.getUser());
            content.setText(comment.getContent());
            containerLayout.addView(item);
        }
        return root;
    }
}
