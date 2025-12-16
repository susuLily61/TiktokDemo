package com.BDhomework.tiktokdemo.comment;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.BDhomework.tiktokdemo.data.repository.FeedRepository;

public class CommentViewModelFactory implements ViewModelProvider.Factory {

    private final FeedRepository repository;

    public CommentViewModelFactory(FeedRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CommentViewModel.class)) {
            return modelClass.cast(new CommentViewModel(repository));
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
