package com.BDhomework.tiktokdemo.data;

import com.BDhomework.tiktokdemo.model.Comment;
import com.BDhomework.tiktokdemo.model.FeedItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

public class MockFeedRepository {

    // 封面池（横图为主没关系，后面可以换成竖图）
    private static final List<String> COVER_POOL = Arrays.asList(
            "https://images.unsplash.com/photo-1507525428034-b723cf961d3e",
            "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee",
            "https://images.unsplash.com/photo-1469474968028-56623f02e42e",
            "https://images.unsplash.com/photo-1501785888041-af3ef285b470",
            "https://images.unsplash.com/photo-1501004318641-b39e6451bec6",
            "https://images.unsplash.com/photo-1489515217757-5fd1be406fef",
            "https://images.unsplash.com/photo-1518837695005-2083093ee35b",
            "https://images.unsplash.com/photo-1487412720507-e7ab37603c6f"
    );

    // 头像池
    private static final List<String> AVATAR_POOL = Arrays.asList(
            "https://images.unsplash.com/profile-1502680755645-4c502e91c95f",
            "https://images.unsplash.com/profile-1520813792240-02dc0ae524b9",
            "https://images.unsplash.com/profile-1441991229545-2de04c01423c",
            "https://images.unsplash.com/profile-1544037971-3a3ec0c49b80"
    );

    // 标题池
    private static final List<String> TITLE_POOL = Arrays.asList(
            "今天的天空好治愈",
            "随机记录一下生活",
            "不小心拍到的绝美一幕",
            "慢节奏的一天",
            "这也太可爱了吧",
            "开工前最后的摸鱼",
            "通勤路上的小确幸",
            "周末追光计划"
    );

    // 描述池
    private static final List<String> DESCRIPTION_POOL = Arrays.asList(
            "愿你有一双匡威，陪你走遍天涯 #匡威1970s @汤圆club",
            "夕阳下的海风刚刚好，下一站去哪里？",
            "城市夜跑的第 12 天，坚持总会有收获。",
            "这一刻想把天空的颜色分享给你们。",
            "周末露营记：烟火气和星空都很治愈。",
            "记录下这一条街的老味道和新气息。"
    );

    // 视频池（竖屏视频可以替换这里的 URL）
    private static final List<String> VIDEO_POOL = Arrays.asList(
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/tiktokedmo/02a39b9a5a7c672bf0e8353f9331f614.mp4",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/tiktokedmo/0550549dc67b9e580c1a97ce1666511a.mp4",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/tiktokedmo/0a3ca4e3cc7338a2b9f4e5bf83816164.mp4",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/tiktokedmo/0fdfc2141cec956c55e14f82781ec229.mp4",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/tiktokedmo/17944fc44775f6ee9a7973b55235faa6.mp4",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/tiktokedmo/4271df520269483b57ec84e33e7bd785.mp4",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/tiktokedmo/5159205ef8d9483d7e6a0dc0266affc1.mp4",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/tiktokedmo/6179fdf874dfae707a064f2223686a4e.mp4",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/tiktokedmo/631425c1c1ae5fbbdfa44f11ca4d21e3.mp4",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/tiktokedmo/674b377036d6b9873aed6c56b44ff05e.mp4",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/tiktokedmo/7018ba37fd3fbea1812c90e1a6dfbe4f.mp4",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/tiktokedmo/aeedaef1ff57b32de1d344317140bc6f.mp4",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/tiktokedmo/c06ca83c7d3790634b6de3e31c4e1101.mp4",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/tiktokedmo/c52226a5b3035077d6a87c89dc638696.mp4",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/tiktokedmo/eb27f011f20c2d5001c61d700e7e21c4.mp4"
    );

    private static final Random RANDOM = new Random();

    public List<FeedItem> loadFeed(int page) {
        List<FeedItem> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String id = UUID.randomUUID().toString();

            // 标题
            String title = TITLE_POOL.get((page * 10 + i) % TITLE_POOL.size());

            // 作者昵称
            String author = String.format(Locale.CHINA, "用户%d", (page * 10) + i + 100);

            // 头像 / 封面 / 视频 从各自池子里轮询或随机取一个
            String avatar = AVATAR_POOL.get(RANDOM.nextInt(AVATAR_POOL.size()));
            String cover = COVER_POOL.get((page * 10 + i) % COVER_POOL.size())
                    + "?auto=format&fit=crop&w=800&q=80";
            String videoUrl = VIDEO_POOL.get((page * 10 + i) % VIDEO_POOL.size());

            // 描述
            String description = DESCRIPTION_POOL.get(i % DESCRIPTION_POOL.size());

            // 点赞数
            int likes = 1000 + (page * 37) + i * 13 + RANDOM.nextInt(500);

            // 注意这里的参数要和 FeedItem 构造函数一一对应
            list.add(new FeedItem(
                    id,
                    title,
                    author,
                    avatar,
                    cover,
                    videoUrl,
                    likes,
                    description
            ));
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