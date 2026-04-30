# English Speaking Practice

基于 Java Swing + FlatLaf 的英语口语练习桌面应用。集成 DeepSeek 大模型、阿里云发音评测、whisper.cpp 语音识别和 edge-tts 神经语音合成。

---

## 功能

| 模块 | 说明 |
|------|------|
| **Reading Test** | 朗读英文文章 → AI 评测发音、流利度 → 教练式反馈 + 音素级评分 |
| **Conversation Practice** | 情景对话（面试/聊天/求助），支持 Hide Text 听力训练模式 |
| **Voice Input** | 麦克风录音，whisper.cpp 实时转文字 |
| **Voice Output** | edge-tts 神经语音朗读，7 种声线 + 5 档语速可调 |
| **Pronunciation Assessment** | 阿里云音素级发音评测（可选，未配置时自动降级） |
| **Typing Animation** | AI 回复打字机逐字跳出，与语音同步 |
| **Warm UI** | FlatLaf 暖色主题，圆角组件，奶油背景 + 琥珀强调色 |

---

## 技术架构

```
用户录音 → whisper.cpp (STT) + 阿里云 (发音评测)
                ↓
         DeepSeek API (教练反馈 / 对话回复)
                ↓
         edge-tts (神经语音朗读) → 扬声器
```

| 组件 | 用途 | 安装方式 |
|------|------|---------|
| JDK ≥ 17 | Java 运行时 | 手动安装 |
| FlatLaf 3.5.4 | 现代化 UI 主题 | 下载 jar |
| Gson 2.11.0 | JSON 解析 | Maven / 下载 jar |
| whisper.cpp | 语音识别 | 下载预编译包 + 模型 |
| edge-tts | 语音合成 | `pip install edge-tts` |
| DeepSeek API | 对话与教练反馈 | 注册获取 API Key |
| 阿里云智能语音 | 发音评测（可选） | 开通服务获取 AccessKey |

---

## 快速开始

### 1. 准备依赖

```bash
# 安装 edge-tts
pip install edge-tts

# 下载 whisper.cpp 模型，放入 STT/ 目录
# ggml-base.en.bin (~141MB): https://huggingface.co/ggerganov/whisper.cpp
# whisper-bin-x64.zip: https://github.com/ggml-org/whisper.cpp/releases
# 解压后将 whisper-cli.exe、ggml.dll 等放入 STT/

# 下载 FlatLaf 和 Gson jar 放入 target/
# flatlaf-3.5.4.jar: https://repo1.maven.org/maven2/com/formdev/flatlaf/3.5.4/
# gson-2.11.0.jar: https://repo1.maven.org/maven2/com/google/code/gson/gson/2.11.0/
```

### 2. 获取 API Key

- **DeepSeek**：https://platform.deepseek.com
- **阿里云 ASR（可选）**：开通智能语音交互服务，获取 AccessKey ID/Secret 和 AppKey

### 3. 编译运行

```bash
# 全量编译
D:\JDK\bin\javac -cp "target\gson-2.11.0.jar;target\flatlaf-3.5.4.jar" ^
  -d target/classes ^
  src/main/java/com/englishspeaker/**/*.java

# 运行
D:\JDK\bin\java -XX:+UseSerialGC -Xmx128m ^
  -cp "target\classes;target\gson-2.11.0.jar;target\flatlaf-3.5.4.jar" ^
  com.englishspeaker.EnglishSpeakerApp
```

或双击 `run.bat`。

### 4. 配置 API

首次启动显示 API 配置页。之后通过主界面 **API Settings** 按钮修改。

---

## 使用说明

### Reading Test
1. 看文章 → 点 🎤 Record 朗读（或打字）
2. 点 Evaluate → AI 返回评分 + 反馈 + 发音详情
3. 🔊 TTS 控制播放，🔁 Replay 重听

### Conversation Practice
1. 选择场景（面试/聊天/求助）和回复长度（Normal/Detailed）
2. 点 Start → AI 开始对话
3. 打字或录音回复 → 打字机动画 + 语音同步播出
4. **🙈 Hide Text** — 隐藏 AI 文字，纯听力训练
5. **Summary** — 对话结束后，查看整场发音分析

---

## 项目结构

```
src/main/java/com/englishspeaker/
├── EnglishSpeakerApp.java       — 主窗口 + FlatLaf 主题
├── gui/
│   ├── SetupPanel.java          — API 配置页（首次启动）
│   ├── HomePanel.java           — 主菜单 + API Settings 入口
│   ├── ReadingPanel.java        — 朗读测试界面
│   └── ConversationPanel.java   — 对话界面（打字机/隐藏文字/加载动画）
├── service/
│   ├── AiService.java           — DeepSeek API
│   ├── AlibabaAsrService.java   — 阿里云发音评测（WebSocket）
│   ├── ReadingService.java      — 朗读评测逻辑
│   ├── ConversationService.java — 对话管理 + 发音总结 + 回复长度控制
│   ├── AudioRecorderService.java— 麦克风录音
│   ├── SpeechToTextService.java — whisper.cpp 调用
│   └── TextToSpeechService.java — edge-tts（预热 + 流式播放）
└── model/
    ├── PhonemeScore.java        — 音素级得分
    ├── WordScore.java           — 单词级得分
    ├── PronunciationResult.java — 发音评测完整结果
    ├── ReadingResult.java       — 朗读评测结果
    ├── ConversationTurn.java    — 对话轮次记录
    └── Scenario.java            — 对话场景枚举
```

---

## 注意事项

- `config.properties` 已加入 `.gitignore`，不会被提交
- 阿里云 ASR 未配置时自动降级为 whisper 转写 + DeepSeek 评分
- 需 `pip install edge-tts` 支持 TTS 语音朗读
