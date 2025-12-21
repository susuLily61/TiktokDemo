package com.BDhomework.tiktokdemo.ai;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.*;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ArkClient {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // 方舟 v3 Chat Completions
    private static final String ENDPOINT =
            "https://ark.cn-beijing.volces.com/api/v3/chat/completions"; // :contentReference[oaicite:4]{index=4}

    private final OkHttpClient http = new OkHttpClient();
    private final Gson gson = new Gson();

    private final String apiKey;
    private final String model;

    public ArkClient(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

    public String chat(List<AiMessage> history) throws IOException {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);

        List<Map<String, String>> msgs = new ArrayList<>();
        for (AiMessage m : history) {
            Map<String, String> one = new HashMap<>();
            one.put("role", m.role);
            one.put("content", m.content);
            msgs.add(one);
        }
        body.put("messages", msgs);

        String json = gson.toJson(body);

        Request req = new Request.Builder()
                .url(ENDPOINT)
                .addHeader("Content-Type", "application/json")
                // Bearer 鉴权 :contentReference[oaicite:5]{index=5}
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(json, JSON))
                .build();

        try (Response resp = http.newCall(req).execute()) {
            String text = resp.body() != null ? resp.body().string() : "";
            if (!resp.isSuccessful()) {
                throw new IOException("HTTP " + resp.code() + " " + text);
            }

            ArkResp r = gson.fromJson(text, ArkResp.class);
            if (r == null || r.choices == null || r.choices.isEmpty()
                    || r.choices.get(0).message == null) {
                throw new IOException("Bad response: " + text);
            }
            return r.choices.get(0).message.content;
        }
    }

    static class ArkResp {
        List<Choice> choices;
    }
    static class Choice {
        Msg message;
    }
    static class Msg {
        String role;
        String content;
    }
}
