package com.BDhomework.tiktokdemo.ui.recommend;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.BDhomework.tiktokdemo.data.repository.FeedRepository;

public class FeedViewModelFactory implements ViewModelProvider.Factory {

    private final FeedRepository repository;

    public FeedViewModelFactory(FeedRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(FeedViewModel.class)) {
            return (T) new FeedViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
