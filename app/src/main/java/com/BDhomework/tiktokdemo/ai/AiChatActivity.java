package com.BDhomework.tiktokdemo.ai;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.BDhomework.tiktokdemo.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.appcompat.widget.Toolbar;

public class AiChatActivity extends AppCompatActivity {

    private static final String MODEL = "ep-20251221212303-bsjd2"; // 你的接入点
    private static final String ARK_API_KEY = "a02f1e24-6cb9-4496-a33d-fd9051098890";

    private static final String SYSTEM_PROMPT =
            "你是一个简洁专业的AI助手。不要角色扮演，不要输出“系统提示/思考过程/推理内容”，"
                    + "只用中文回答用户问题，回答尽量简短、直接。";

    private ArkClient client;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final List<AiMessage> messages = new ArrayList<>();
    private AiChatAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        Toolbar toolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        client = new ArkClient(ARK_API_KEY, MODEL);

        RecyclerView list = findViewById(R.id.chat_list);
        EditText input = findViewById(R.id.chat_input);
        Button send = findViewById(R.id.chat_send);

        adapter = new AiChatAdapter(messages);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        list.setLayoutManager(lm);
        list.setAdapter(adapter);

        send.setOnClickListener(v -> {
            String text = input.getText().toString().trim();
            if (text.isEmpty()) return;

            input.setText("");

            // 1) 本地先显示用户消息
            messages.add(new AiMessage("user", text));
            adapter.notifyItemInserted(messages.size() - 1);
            list.scrollToPosition(messages.size() - 1);

            // 2) 发请求（可先插入一条“思考中...”）
            int typingIndex = messages.size();
            messages.add(new AiMessage("assistant", "思考中…"));
            adapter.notifyItemInserted(messages.size() - 1);
            list.scrollToPosition(messages.size() - 1);

            // 3) 异步请求
            List<AiMessage> snapshot = new ArrayList<>(messages);
            executor.execute(() -> {
                try {
                    int N = 12;

                    // 1) 截取最近 N 条（注意：不要把“思考中...”那条发给模型）
                    List<AiMessage> recentRaw = snapshot.size() > N
                            ? snapshot.subList(snapshot.size() - N, snapshot.size())
                            : snapshot;

                    // 2) 重新组装：system + 历史（过滤掉 content="思考中…" 的占位）
                    List<AiMessage> requestMessages = new ArrayList<>();
                    requestMessages.add(new AiMessage("system", SYSTEM_PROMPT));

                    for (AiMessage m : recentRaw) {
                        if ("assistant".equals(m.role) && "思考中…".equals(m.content)) continue;
                        requestMessages.add(m);
                    }

                    String reply = client.chat(requestMessages);

                    runOnUiThread(() -> {
                        messages.set(typingIndex, new AiMessage("assistant", reply));
                        adapter.notifyItemChanged(typingIndex);
                        list.scrollToPosition(messages.size() - 1);
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        messages.set(typingIndex, new AiMessage("assistant", "出错了：" + e.getMessage()));
                        adapter.notifyItemChanged(typingIndex);
                        Toast.makeText(this, "AI 请求失败", Toast.LENGTH_SHORT).show();
                    });
                }
            });

        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
