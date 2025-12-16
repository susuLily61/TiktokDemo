package com.BDhomework.tiktokdemo.comment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.BDhomework.tiktokdemo.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Objects;

public class CommentBottomSheetDialog extends BottomSheetDialogFragment {

    private static final String ARG_FEED_ID = "arg_feed_id";

    public interface OnCommentAddedListener {
        void onCommentAdded(int totalCount);
    }

    private OnCommentAddedListener onCommentAddedListener;
    private CommentViewModelFactory viewModelFactory;
    private CommentViewModel viewModel;
    private CommentAdapter adapter;
    private String feedId;
    private ProgressBar loadingView;
    private TextView errorView;
    private EditText inputView;
    private TextView sendView;
    private int lastCommentCount = 0;

    public static CommentBottomSheetDialog newInstance(String feedId) {
        CommentBottomSheetDialog dialog = new CommentBottomSheetDialog();
        Bundle args = new Bundle();
        args.putString(ARG_FEED_ID, feedId);
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnCommentAddedListener(OnCommentAddedListener listener) {
        this.onCommentAddedListener = listener;
    }

    public void setViewModelFactory(CommentViewModelFactory factory) {
        this.viewModelFactory = factory;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comment_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        feedId = getArguments() != null ? getArguments().getString(ARG_FEED_ID, "") : "";
        setupViewModel();
        setupViews(view);
        observeState();
        viewModel.loadInitial(feedId);
    }

    private void setupViews(View root) {
        RecyclerView recyclerView = root.findViewById(R.id.comment_list);
        loadingView = root.findViewById(R.id.comment_loading);
        errorView = root.findViewById(R.id.comment_error);
        inputView = root.findViewById(R.id.comment_input);
        sendView = root.findViewById(R.id.comment_send);

        adapter = new CommentAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0) return;
                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (lm == null) return;
                int visible = lm.getChildCount();
                int total = lm.getItemCount();
                int firstVisible = lm.findFirstVisibleItemPosition();
                if (visible + firstVisible >= total - 1) {
                    viewModel.loadMore(feedId);
                }
            }
        });

        sendView.setOnClickListener(v -> {
            String text = inputView.getText().toString();
            if (TextUtils.isEmpty(text.trim())) return;
            viewModel.send(feedId, text);
            inputView.setText("");
        });
    }

    private void setupViewModel() {
        ViewModelProvider.Factory factory = Objects.requireNonNull(viewModelFactory, "CommentViewModelFactory is required");
        viewModel = new ViewModelProvider(this, factory).get(CommentViewModel.class);
    }

    private void observeState() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            adapter.submitList(state.getComments());
            loadingView.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);
            errorView.setVisibility(state.getError() == null ? View.GONE : View.VISIBLE);
            errorView.setText(state.getError());

            if (onCommentAddedListener != null && state.getComments().size() != lastCommentCount) {
                lastCommentCount = state.getComments().size();
                onCommentAddedListener.onCommentAdded(lastCommentCount);
            }
        });
    }
}
