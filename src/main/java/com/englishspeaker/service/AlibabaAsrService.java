package com.englishspeaker.service;

import com.englishspeaker.model.PhonemeScore;
import com.englishspeaker.model.PronunciationResult;
import com.englishspeaker.model.WordScore;
import com.google.gson.*;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.Files;
import java.net.http.WebSocket.*;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

public class AlibabaAsrService {
    private final String accessKeyId;
    private final String accessKeySecret;
    private final String appKey;
    private final boolean enabled;
    private final HttpClient httpClient;
    private final Gson gson = new Gson();

    private String cachedToken;
    private long tokenExpiry;
    private final Object tokenLock = new Object();

    public AlibabaAsrService() throws IOException {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("config.properties")) {
            props.load(in);
        }
        this.accessKeyId = props.getProperty("aliyun.accessKeyId", "").trim();
        this.accessKeySecret = props.getProperty("aliyun.accessKeySecret", "").trim();
        this.appKey = props.getProperty("aliyun.appKey", "").trim();
        this.enabled = !accessKeyId.isEmpty() && !accessKeySecret.isEmpty() && !appKey.isEmpty();
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public boolean isEnabled() { return enabled; }

    public PronunciationResult evaluate(File wavFile, String referenceText) {
        if (!enabled) return null;

        try {
            String token = getToken();
            return callAssessment(token, wavFile, referenceText);
        } catch (Exception e) {
            System.err.println("Alibaba ASR error: " + e.getMessage());
            return null;
        }
    }

    private String getToken() throws AlibabaAsrException {
        synchronized (tokenLock) {
            if (cachedToken != null && System.currentTimeMillis() < tokenExpiry - 300_000) {
                return cachedToken;
            }
            return refreshToken();
        }
    }

    private String refreshToken() throws AlibabaAsrException {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("access_key_id", accessKeyId);
            body.addProperty("access_key_secret", accessKeySecret);
            body.addProperty("app_key", appKey);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://nls-meta.cn-shanghai.aliyuncs.com/api/v1/token"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new AlibabaAsrException("Token request failed: " + resp.statusCode());
            }

            JsonObject json = JsonParser.parseString(resp.body()).getAsJsonObject();
            cachedToken = json.get("token").getAsString();
            long expireSec = json.get("expire_time").getAsLong();
            tokenExpiry = System.currentTimeMillis() + expireSec * 1000;
            return cachedToken;

        } catch (IOException | InterruptedException e) {
            throw new AlibabaAsrException("Token request error", e);
        }
    }

    private PronunciationResult callAssessment(String token, File wavFile, String refText)
            throws Exception {

        CompletableFuture<PronunciationResult> future = new CompletableFuture<>();
        String taskId = UUID.randomUUID().toString();
        byte[] audioData = Files.readAllBytes(wavFile.toPath());

        WebSocket ws = httpClient.newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .buildAsync(URI.create(
                        "wss://nls-gateway.cn-shanghai.aliyuncs.com/ws/v1?token=" + token),
                        new Listener() {
                            boolean startSent = false;
                            boolean audioSent = false;
                            StringBuilder responseText = new StringBuilder();

                            @Override
                            public void onOpen(WebSocket webSocket) {
                                String startMsg = buildStartRequest(taskId, refText);
                                webSocket.sendText(startMsg, true);
                                startSent = true;
                            }

                            @Override
                            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                                String msg = data.toString();
                                if (msg.contains("\"TranscriptionStarted\"") && !audioSent) {
                                    audioSent = true;
                                    // Send audio in chunks
                                    int offset = 0;
                                    while (offset < audioData.length) {
                                        int len = Math.min(8192, audioData.length - offset);
                                        webSocket.sendBinary(ByteBuffer.wrap(audioData, offset, len), true);
                                        offset += len;
                                    }
                                    // Send stop
                                    webSocket.sendText(buildStopRequest(taskId), true);
                                }
                                if (msg.contains("\"TranscriptionResultChanged\"")
                                        || msg.contains("\"TranscriptionCompleted\"")) {
                                    responseText.append(msg);
                                }
                                if (msg.contains("\"TranscriptionCompleted\"")) {
                                    webSocket.sendClose(1000, "done");
                                    try {
                                        PronunciationResult result = parseResult(responseText.toString());
                                        future.complete(result);
                                    } catch (Exception e) {
                                        future.completeExceptionally(e);
                                    }
                                }
                                return null;
                            }

                            @Override
                            public void onError(WebSocket webSocket, Throwable error) {
                                future.completeExceptionally(error);
                            }
                        }).get(10, TimeUnit.SECONDS);

        return future.get(30, TimeUnit.SECONDS);
    }

    private String buildStartRequest(String taskId, String refText) {
        JsonObject header = new JsonObject();
        header.addProperty("name", "StartTranscription");
        header.addProperty("namespace", "SpeechTranscriber");
        header.addProperty("task_id", taskId);
        header.addProperty("appkey", appKey);

        JsonObject payload = new JsonObject();
        payload.addProperty("format", "wav");
        payload.addProperty("sample_rate", 16000);
        payload.addProperty("enable_intermediate_result", false);
        payload.addProperty("max_sentence_silence", 800);

        // Pronunciation assessment parameters
        JsonObject assessment = new JsonObject();
        assessment.addProperty("enable", true);
        assessment.addProperty("core_type", "en.sent.score");
        payload.add("speech_assessment", assessment);
        payload.addProperty("speech_noise_threshold", 0.1);

        JsonObject root = new JsonObject();
        root.add("header", header);
        root.add("payload", payload);
        return gson.toJson(root);
    }

    private String buildStopRequest(String taskId) {
        JsonObject header = new JsonObject();
        header.addProperty("name", "StopTranscription");
        header.addProperty("namespace", "SpeechTranscriber");
        header.addProperty("task_id", taskId);

        JsonObject root = new JsonObject();
        root.add("header", header);
        return gson.toJson(root);
    }

    private PronunciationResult parseResult(String responseJson) {
        // 从 WebSocket 返回的多条消息中提取最终评测结果
        JsonObject root = JsonParser.parseString(responseJson).getAsJsonObject();
        PronunciationResult result = new PronunciationResult();

        // 尝试从 payload 中提取评测数据
        JsonObject payload = null;
        if (root.has("payload")) {
            payload = root.getAsJsonObject("payload");
        }

        if (payload != null && payload.has("result")) {
            String resultStr = payload.get("result").getAsString();
            try {
                JsonObject resultJson = JsonParser.parseString(resultStr).getAsJsonObject();
                if (resultJson.has("speech_assessment")) {
                    JsonObject assessment = resultJson.getAsJsonObject("speech_assessment");
                    result.setOverallScore(getIntField(assessment, "total_score", -1));
                    result.setPronunciationScore(getIntField(assessment, "phone_score", -1));
                    result.setFluencyScore(getIntField(assessment, "fluency_score", -1));
                    result.setCompletenessScore(getIntField(assessment, "integrity_score", -1));

                    // 解析单词级分数
                    if (assessment.has("details")) {
                        List<WordScore> wordScores = parseWordScores(assessment.getAsJsonArray("details"));
                        result.setWordScores(wordScores);
                    }
                }
            } catch (JsonSyntaxException e) {
                // result 可能是 JSON 字符串，再次解析失败则忽略
            }
        }

        return result;
    }

    private List<WordScore> parseWordScores(JsonArray details) {
        List<WordScore> list = new ArrayList<>();
        for (JsonElement elem : details) {
            JsonObject obj = elem.getAsJsonObject();
            WordScore ws = new WordScore();
            ws.setWord(obj.has("word") ? obj.get("word").getAsString() : "");
            ws.setScore(getIntField(obj, "score", -1));

            List<PhonemeScore> phonemes = new ArrayList<>();
            if (obj.has("phones")) {
                JsonArray phones = obj.getAsJsonArray("phones");
                for (int i = 0; i < phones.size(); i++) {
                    JsonObject p = phones.get(i).getAsJsonObject();
                    PhonemeScore ps = new PhonemeScore();
                    ps.setPhoneme(p.has("phone") ? p.get("phone").getAsString() : "");
                    ps.setScore(getIntField(p, "score", -1));
                    phonemes.add(ps);
                }
            }
            ws.setPhonemeScores(phonemes);
            list.add(ws);
        }
        return list;
    }

    private int getIntField(JsonObject obj, String key, int defaultValue) {
        if (obj.has(key)) {
            try { return obj.get(key).getAsInt(); } catch (Exception e) { return defaultValue; }
        }
        return defaultValue;
    }
}
