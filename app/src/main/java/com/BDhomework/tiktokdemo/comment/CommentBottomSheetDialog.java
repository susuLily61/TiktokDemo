package com.BDhomework.tiktokdemo.comment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.BDhomework.tiktokdemo.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Objects;

public class CommentBottomSheetDialog extends BottomSheetDialogFragment {

    private static final String ARG_FEED_ID = "arg_feed_id";
    private static final String ARG_COMMENT_TOTAL = "arg_comment_total";

    public interface OnCommentAddedListener {
        void onCommentAdded(int totalCount);
    }

    private OnCommentAddedListener onCommentAddedListener;
    private CommentViewModelFactory viewModelFactory;
    private CommentViewModel viewModel;
    private CommentAdapter adapter;

    private String feedId;
    private int totalCount = 0;

    private ProgressBar loadingView;
    private TextView errorView;
    private EditText inputView;
    private TextView sendView;

    private TextView titleView;
    private View btnExpand;
    private View btnClose;

    private BottomSheetBehavior<?> behavior;

    // 用来判断“是否新增评论”
    private int lastLoadedCount = 0;

    private void setExpandedOffsetCompat(BottomSheetBehavior<?> behavior, int offset) {
        try {
            java.lang.reflect.Method m =
                    BottomSheetBehavior.class.getMethod("setExpandedOffset", int.class);
            m.invoke(behavior, offset);
        } catch (Throwable ignored) {
        }
    }

    public static CommentBottomSheetDialog newInstance(String feedId, int totalCount) {
        CommentBottomSheetDialog dialog = new CommentBottomSheetDialog();
        Bundle args = new Bundle();
        args.putString(ARG_FEED_ID, feedId);
        args.putInt(ARG_COMMENT_TOTAL, totalCount);
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnCommentAddedListener(OnCommentAddedListener listener) {
        this.onCommentAddedListener = listener;
    }

    public void setViewModelFactory(CommentViewModelFactory factory) {
        this.viewModelFactory = factory;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!(getDialog() instanceof BottomSheetDialog)) return;

        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        FrameLayout sheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (sheet == null) return;

        BottomSheetBehavior<FrameLayout> b = BottomSheetBehavior.from(sheet);
        this.behavior = b;

        sheet.post(() -> {
            View parent = (View) sheet.getParent();
            int parentH = parent.getHeight();
            if (parentH <= 0) return;

            int peek = (int) (parentH * 0.5f); // 初始 50%

            b.setFitToContents(false);
            setExpandedOffsetCompat(b, 0);

            b.setPeekHeight(peek);
            b.setState(BottomSheetBehavior.STATE_COLLAPSED);

            b.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) { }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    int visibleHeight = parentH - bottomSheet.getTop();
                    if (visibleHeight < peek) visibleHeight = peek;
                    setSheetHeight(bottomSheet, visibleHeight);
                }
            });

            setSheetHeight(sheet, peek);
        });
    }

    private void setSheetHeight(View bottomSheet, int height) {
        ViewGroup.LayoutParams lp = bottomSheet.getLayoutParams();
        if (lp.height != height) {
            lp.height = height;
            bottomSheet.setLayoutParams(lp);
        }
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
        totalCount = getArguments() != null ? getArguments().getInt(ARG_COMMENT_TOTAL, 0) : 0;

        setupViewModel();
        setupViews(view);

        // 初始标题用“外部传入的总数”
        titleView.setText(totalCount + "条评论");

        observeState();
        viewModel.loadInitial(feedId);
    }

    private void setupViews(View root) {
        RecyclerView recyclerView = root.findViewById(R.id.comment_list);
        loadingView = root.findViewById(R.id.comment_loading);
        errorView = root.findViewById(R.id.comment_error);
        inputView = root.findViewById(R.id.comment_input);
        sendView = root.findViewById(R.id.comment_send);

        titleView = root.findViewById(R.id.comment_title);
        btnExpand = root.findViewById(R.id.comment_btn_expand);
        btnClose = root.findViewById(R.id.comment_btn_close);

        btnClose.setOnClickListener(v -> dismiss());

        btnExpand.setOnClickListener(v -> {
            if (behavior != null) {
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

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
        ViewModelProvider.Factory factory =
                Objects.requireNonNull(viewModelFactory, "CommentViewModelFactory is required");
        viewModel = new ViewModelProvider(this, factory).get(CommentViewModel.class);
    }

    private void observeState() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            adapter.submitList(state.getComments());

            loadingView.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);
            errorView.setVisibility(state.getError() == null ? View.GONE : View.VISIBLE);
            errorView.setText(state.getError());

            int loadedNow = state.getComments().size();

            if (loadedNow > lastLoadedCount && loadedNow > 0) {
                String firstUser = state.getComments().get(0).getUser();
                if ("我".equals(firstUser)) {
                    totalCount += (loadedNow - lastLoadedCount);
                    titleView.setText(totalCount + "条评论");

                    if (onCommentAddedListener != null) {
                        onCommentAddedListener.onCommentAdded(totalCount);
                    }
                }
            }

            lastLoadedCount = loadedNow;
        });
    }
}
