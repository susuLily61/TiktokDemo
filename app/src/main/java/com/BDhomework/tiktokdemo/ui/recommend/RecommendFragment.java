package com.BDhomework.tiktokdemo.ui.recommend;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.BDhomework.tiktokdemo.R;
import com.BDhomework.tiktokdemo.data.repository.impl.MockFeedRepository;
import com.BDhomework.tiktokdemo.model.FeedItem;
import com.BDhomework.tiktokdemo.player.VideoFeedActivity;

import java.util.ArrayList;
import java.util.List;

public class RecommendFragment extends Fragment implements FeedAdapter.OnFeedClickListener {

    private FeedViewModel viewModel;
    private FeedAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    public static RecommendFragment newInstance() {
        return new RecommendFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_recommend, container, false);
        swipeRefreshLayout = root.findViewById(R.id.feed_refresh);
        RecyclerView recyclerView = root.findViewById(R.id.feed_list);
        ProgressBar footerProgress = root.findViewById(R.id.feed_footer_progress);

        adapter = new FeedAdapter(this);
        recyclerView.setAdapter(adapter);

        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        recyclerView.setLayoutManager(layoutManager);

        // 非固定大小，允许 item 高度根据图片变化
        recyclerView.setHasFixedSize(false);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                FeedUiState state = viewModel.getUiState().getValue();
                boolean loadingMore = state != null && state.isLoadingMore();
                if (dy > 0 && !loadingMore) {
                    StaggeredGridLayoutManager manager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
                    if (manager != null) {
                        int[] lastPositions = manager.findLastVisibleItemPositions(null);
                        int last = Math.max(lastPositions[0], lastPositions[1]);
                        if (last >= adapter.getItemCount() - 3) {
                            viewModel.loadMore();
                        }
                    }
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> viewModel.refresh());

        FeedViewModelFactory factory = new FeedViewModelFactory(new MockFeedRepository());
        viewModel = new ViewModelProvider(this, factory).get(FeedViewModel.class);
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            adapter.submitList(state.getItems());
            swipeRefreshLayout.setRefreshing(state.isRefreshing());
            footerProgress.setVisibility(state.isLoadingMore() ? View.VISIBLE : View.GONE);

            if (state.getErrorMessage() != null) {
                Toast.makeText(requireContext(), state.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        FeedUiState currentState = viewModel.getUiState().getValue();
        if (currentState == null || currentState.getItems().isEmpty()) {
            swipeRefreshLayout.setRefreshing(true);
            viewModel.refresh();
        }

        return root;
    }

    @Override
    public void onFeedClick(View sharedView, int position, FeedItem item) {
        FeedUiState current = viewModel.getUiState().getValue();
        if (current != null) {
            Intent intent = new Intent(requireContext(), VideoFeedActivity.class);
            intent.putExtra(VideoFeedActivity.EXTRA_FEED_LIST, new ArrayList<>(current.getItems()));
            intent.putExtra(VideoFeedActivity.EXTRA_FEED_ID, item.getId());
            intent.putExtra(VideoFeedActivity.EXTRA_FEED_POSITION, position);

            // 使用 shared element 启动 VideoFeedActivity
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                            requireActivity(),
                            sharedView,
                            sharedView.getTransitionName()   // 与 item_feed_card.xml / 视频页一致
                    );

            startActivity(intent, options.toBundle());
        }
    }

}
