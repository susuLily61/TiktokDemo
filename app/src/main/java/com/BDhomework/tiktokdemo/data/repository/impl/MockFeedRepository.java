package com.BDhomework.tiktokdemo.data.repository.impl;

import com.BDhomework.tiktokdemo.data.repository.CommentPage;
import com.BDhomework.tiktokdemo.data.repository.FeedRepository;
import com.BDhomework.tiktokdemo.model.Comment;
import com.BDhomework.tiktokdemo.model.FeedItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class MockFeedRepository implements FeedRepository {

    // 封面池
    private static final List<String> COVER_POOL = Arrays.asList(
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/douyincover/01yumaoqiu.png",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/douyincover/022kaice.png",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/douyincover/02wangshun.png",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/douyincover/03kuzhi.png",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/douyincover/05quanhongcan.png",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/douyincover/06qiao.png",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/douyincover/07chang.png",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/douyincover/08jiguang.png",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/douyincover/09tangqian.png"
    );

    // 头像池
    private static final List<String> AVATAR_POOL = Arrays.asList(
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/%E5%A4%B4%E5%83%8F1.jpg",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/%E5%A4%B4%E5%83%8F2.jpg",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/%E5%A4%B4%E5%83%8F3.jpg",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/%E5%A4%B4%E5%83%8F5.jpg",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/%E5%A4%B4%E5%83%8F6.jpg",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/%E5%A4%B4%E5%83%8F7.jpg",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/%E5%A4%B4%E5%83%8F8.jpg",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/%E5%A4%B4%E5%83%8F9.jpg",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/%E5%A4%B4%E5%83%8F10.jpg"
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
            "周末追光计划",
            "风是自由的"
    );

    // 描述池
    private static final List<String> DESCRIPTION_POOL = Arrays.asList(
            "以前总觉得‘长大’这两个字很孤独，连偏旁部首都没有。后来才发现，它需要我们自己一笔一画写出来。 @汤圆club",
            "生活不会永远顺着我们，但总会有一些不期而遇的温暖，让我们原谅了生活所有的刁难。",
            "听说这个BGM和运镜是绝配？我不信！我也来试试！#运镜挑战 #听说这样拍会火",
            "原版是天花板？那我这个‘地板砖’版请收好！@好友 来接力！ #表情包模仿秀",
            "活了20多年才知道，这个原来要这样做！别再浪费钱/时间了！#生活小妙招 #干货",
            "“你是不是也以为……？其实90%的人都做错了！30秒告诉你正确答案。",
            "把普通的日子，过得浪漫一些。今日份的快乐已充值完成！",
            "本以为我的生活是励志剧，结果活成了喜剧，偶尔还是悬疑剧（东西去哪了？）。这就是我，不一样的烟火！#当代年轻人的生活现状",
            "记录下这一条街的老味道和新气息。"
    );

    // 视频池（竖屏视频可以替换这里的 URL）
    private static final List<String> VIDEO_POOL = Arrays.asList(
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/douyinvideo/01yumaoqiu.mp4",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/douyinvideo/022kaice.mp4",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/douyinvideo/02wangshun.mp4",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/douyinvideo/03kuzhi.mp4",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/douyinvideo/05quanhongcan.mp4",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/douyinvideo/06qiao.mp4",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/douyinvideo/07chang.mp4",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/douyinvideo/08jiguang.mp4",
            "https://tiktokdemo.oss-cn-qingdao.aliyuncs.com/douyinvideo/09tangqian.mp4"
    );

    private static final Random RANDOM = new Random();
    // 这里存“所有固定的 mock 数据”
    private static final List<FeedItem> ALL_FEED = new ArrayList<>();

    private static final List<Comment> BASE_COMMENTS = Arrays.asList(
            new Comment("小明", "太酷了，想去现场看看！"),
            new Comment("Ava", "画质也太好了吧"),
            new Comment("看见超宇宙SuperCosmic", "有海的城市就是要浪漫一些，双十一忙完给自己放了个小假"),
            new Comment("布讲栗猫", "真想跪下来求自己别买了。。"),
            new Comment("旅行者", "下次开团啥时候！"),
            new Comment("Bigbibi", "网络不好，还不赶快上链接，直接下播了"),
            new Comment("1-7Bread", "记得补充字幕~"),
            new Comment("旅途中的猫", "可爱，知道要过圣诞了，快穿上圣诞的衣服，别冻着孩子"),
            new Comment("夏日", "救命！这小薯的背影已经萌晕我了！它的衣服们看起来都软fufu的"),
            new Comment("小然的自我修养", "有没有好心人告诉我这是哪家餐厅呀，这个拌饭看起来好好吃"),
            new Comment("晚风", "啊啊啊啊啊啊人和衣服都好漂亮！我已经期待一月去哈尔滨穿上了！请出教程！")
    );

    private static final Map<String, List<Comment>> COMMENT_STORAGE = new HashMap<>();

    private static String randomDate() {
        Calendar cal = Calendar.getInstance();
        long end = cal.getTimeInMillis();

        cal.set(2022, Calendar.JANUARY, 1);
        long start = cal.getTimeInMillis();

        long randomTime = start + (long) (RANDOM.nextDouble() * (end - start));
        cal.setTimeInMillis(randomTime);

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        return String.format(Locale.CHINA, "%04d.%02d.%02d", year, month, day);
    }

    static {

        int totalCount = 30;

        for (int index = 0; index < totalCount; index++) {
            String id = "item_" + index;

            String title = TITLE_POOL.get(index % TITLE_POOL.size());

            String author = String.format(Locale.CHINA, "用户%d", index + 100);

            // 封面 / 视频 / 头像 都按 index 做一个稳定的映射
            String cover = COVER_POOL.get(index % COVER_POOL.size())
                    + "?auto=format&fit=crop&w=800&q=80";

            String videoUrl = VIDEO_POOL.get(index % VIDEO_POOL.size());

            String avatar = AVATAR_POOL.get(index % AVATAR_POOL.size());

            String description = DESCRIPTION_POOL.get(index % DESCRIPTION_POOL.size());

            // 点赞数用一个“看起来像随机的”纯函数，跟 index 绑定，而不是 RANDOM
            int likes = 1000 + (index * 37) % 5000;  // 不用 nextInt，保证固定

            String date = randomDate();
            FeedItem item = new FeedItem(
                    id,
                    title,
                    author,
                    avatar,
                    cover,
                    videoUrl,
                    likes,
                    description,
                    date
            );

            ALL_FEED.add(item);
        }
    }


    private static int rotateIndex = 0;

    @Override
    public List<FeedItem> loadFeed(int page) {
        List<FeedItem> rotated = new ArrayList<>(ALL_FEED);
        int total = rotated.size();

        rotateIndex = (rotateIndex + 3) % total; // 每次刷新偏移 3

        Collections.rotate(rotated, rotateIndex);

        return rotated;
    }

    private List<Comment> getComments(String feedId) {
        return COMMENT_STORAGE.computeIfAbsent(feedId, id -> {
            List<Comment> list = new ArrayList<>();
            for (int i = 0; i < 3; i++) list.addAll(BASE_COMMENTS);
            //保证不同视频评论区不一样，但此视频一样，因为按id打乱
            Collections.shuffle(list, new Random(id.hashCode()));
            return list;
        });
    }


    @Override
    public CommentPage loadComments(String feedId, int page, int pageSize) {
        List<Comment> comments = getComments(feedId);
        int start = page * pageSize;
        if (start >= comments.size()) {
            return new CommentPage(Collections.emptyList(), false);
        }
        int end = Math.min(start + pageSize, comments.size());
        boolean hasMore = end < comments.size();
        return new CommentPage(new ArrayList<>(comments.subList(start, end)), hasMore);
    }

    @Override
    public Comment sendComment(String feedId, String content) {
        Comment comment = new Comment("我", content);
        getComments(feedId).add(0, comment);
        return comment;
    }

}
