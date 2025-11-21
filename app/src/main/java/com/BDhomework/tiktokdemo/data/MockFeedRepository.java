package com.BDhomework.tiktokdemo.data;

import com.BDhomework.tiktokdemo.model.Comment;
import com.BDhomework.tiktokdemo.model.FeedItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MockFeedRepository {

    private static final List<String> COVER_POOL = Arrays.asList(
            "https://images.unsplash.com/photo-1507525428034-b723cf961d3e",
            "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee",
            "https://images.unsplash.com/photo-1469474968028-56623f02e42e",
            "https://images.unsplash.com/photo-1501785888041-af3ef285b470",
            "https://images.unsplash.com/photo-1501004318641-b39e6451bec6",
            "https://images.unsplash.com/photo-1489515217757-5fd1be406fef"
    );

    private static final String SAMPLE_VIDEO = "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";

    public List<FeedItem> loadFeed(int page) {
        List<FeedItem> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String id = UUID.randomUUID().toString();
            String title = String.format(Locale.CHINA, "热门短视频 · %d", (page * 10) + i + 1);
            String author = "用户" + ((page * 10) + i + 100);
            String cover = COVER_POOL.get(i % COVER_POOL.size()) + "?auto=format&fit=crop&w=800&q=80";
            int likes = 1000 + (page * 37) + i * 13;
            list.add(new FeedItem(id, title, author,
                    "https://images.unsplash.com/profile-1502680755645-4c502e91c95f",
                    cover,
                    SAMPLE_VIDEO,
                    likes));
        }
        return list;
    }

    public List<Comment> mockComments() {
        return Arrays.asList(
                new Comment("小明", "太酷了，想去现场看看！"),
                new Comment("Ava", "画质也太好了吧"),
                new Comment("Coder", "试试看 ExoPlayer 体验如何"),
                new Comment("路人甲", "双击点赞的动画可以优化~"),
                new Comment("旅行者", "封面配色很舒服")
        );
    }
}
