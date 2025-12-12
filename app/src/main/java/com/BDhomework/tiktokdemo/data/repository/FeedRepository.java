package com.BDhomework.tiktokdemo.data.repository;

import com.BDhomework.tiktokdemo.model.FeedItem;

import java.util.List;

public interface FeedRepository {
    List<FeedItem> loadFeed(int page);
}
