# English Speaking Practice

基于 Java Swing 的英语口语练习桌面应用，集成 DeepSeek 大模型、阿里云发音评测、whisper.cpp 语音识别和 edge-tts 神经语音合成。

---

## 功能

| 模块 | 说明 |
|------|------|
| **Reading Test** | 朗读英文文章 → AI 评测发音、流利度 → 教练式反馈 |
| **Conversation Practice** | 情景对话（面试/聊天/求助）→ AI 实时对话 → 结束后发音总结 |
| **Voice Input** | 麦克风录音，自动识别转文字 |
| **Voice Output** | AI 回复以自然语音朗读，支持多种声线和语速 |
| **Pronunciation Assessment** | 阿里云音素级发音评测（可选，未配置时自动降级） |

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
| JDK ≥ 17 | Java 运行环境 | 手动安装 |
| Gson | JSON 解析 | Maven 自动下载 |
| whisper.cpp | 语音识别 | 下载预编译包 + 模型 |
| edge-tts | 语音合成 | `pip install edge-tts` |
| DeepSeek API | 对话与教练反馈 | 注册获取 API Key |
| 阿里云智能语音 | 发音评测（可选）| 开通服务获取 AccessKey |

---

## 快速开始

### 1. 准备依赖

```bash
# 安装 edge-tts（TTS）
pip install edge-tts

# 下载 whisper.cpp 模型，放入 STT/ 目录
# 从 https://huggingface.co/ggerganov/whisper.cpp 下载 ggml-base.en.bin (~141MB)
# 从 https://github.com/ggml-org/whisper.cpp/releases 下载 whisper-bin-x64.zip
# 解压后将 whisper-cli.exe、ggml.dll 等放入 STT/
```

### 2. 获取 API Key

- **DeepSeek**：在 https://platform.deepseek.com 注册获取
- **阿里云 ASR（可选）**：开通智能语音交互服务，获取 AccessKey ID/Secret 和 AppKey

### 3. 运行

```bash
# 编译
D:\JDK\bin\javac -cp "target\gson-2.11.0.jar" -d target/classes src/main/java/com/englishspeaker/**/*.java

# 复制 Gson 到 target
copy target\gson-2.11.0.jar target\classes\ && cd target\classes && jar xf gson-2.11.0.jar && del gson-2.11.0.jar && cd ..\..

# 打包
D:\JDK\bin\jar cfe target\EnglishSpeaker.jar com.englishspeaker.EnglishSpeakerApp -C target\classes .

# 运行
D:\JDK\bin\java -jar target\EnglishSpeaker.jar
```

或双击 `run.bat`。

### 4. 配置 API

首次启动显示 API 配置页，填写 DeepSeek API URL 和 Key。阿里云 ASR 可选。之后可通过主界面 "API Settings" 按钮修改。

---

## 项目结构

```
src/main/java/com/englishspeaker/
├── EnglishSpeakerApp.java       — 主窗口，CardLayout 切换面板
├── gui/
│   ├── SetupPanel.java          — API 配置页（首次启动）
│   ├── HomePanel.java           — 主菜单
│   ├── ReadingPanel.java        — 朗读测试界面
│   └── ConversationPanel.java   — 情景对话界面
├── service/
│   ├── AiService.java           — DeepSeek API 调用
│   ├── AlibabaAsrService.java   — 阿里云发音评测
│   ├── ReadingService.java      — 朗读评测逻辑
│   ├── ConversationService.java — 对话管理 + 发音总结
│   ├── AudioRecorderService.java— 麦克风录音（javax.sound.sampled）
│   ├── SpeechToTextService.java — whisper.cpp 调用
│   └── TextToSpeechService.java — edge-tts 调用
└── model/
    ├── ReadingResult.java       — 评测结果
    ├── PronunciationResult.java — 发音评测结果
    ├── WordScore.java           — 单词级得分
    ├── PhonemeScore.java        — 音素级得分
    ├── ConversationTurn.java    — 对话轮次记录
    └── Scenario.java            — 对话场景枚举
```

---

## 注意事项

- `config.properties` 包含 API Key，**已被 .gitignore 排除，不会提交到 Git**
- 需 `edge-tts` 环境支持 TTS
- 阿里云 ASR 未配置时，自动降级为 whisper 转写 + DeepSeek 评分
