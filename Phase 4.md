# Phase 4：TTS 语音增强

## 目标

替换 Windows 原生生硬语音，使用 edge-tts 神经语音引擎，让 AI 朗读更自然。同时增加语速控制和音频缓冲修复。

---

## 改动一览

| 项目 | 变更 |
|------|------|
| TTS 引擎 | **Windows SAPI → edge-tts**（微软神经语音）|
| 语音选择 | 新增 7 种自然语音可选 |
| 语速控制 | 0.5x ~ 1.5x 五档可调 |
| 开头丢字修复 | 流式播放 → 先生成文件再播放 |
| 依赖 | 新增 Python 包：`edge-tts` |

---

## edge-tts 介绍

edge-tts 调用 **微软 Edge 浏览器背后的在线神经语音引擎**，免费、无需 API Key、声音自然。

```bash
pip install edge-tts
```

### 命令行用法

```bash
# 列出所有语音
edge-tts --list-voices

# 生成音频文件
edge-tts --voice en-US-AriaNeural --text "Hello" --write-media output.mp3

# 调节语速
edge-tts --voice en-US-AriaNeural --rate=-25% --text "Hello" --write-media output.mp3

# 直接播放
edge-playback --voice en-US-AriaNeural --text "Hello"
```

---

## TextToSpeechService.java 升级

### 语音选择

7 种自然语音，覆盖男女声和不同风格：

| 显示名称 | 语音 ID | 风格 |
|---------|---------|------|
| Aria (女声, 自信) | en-US-AriaNeural | 自信清晰 |
| Jenny (女声, 友善) | en-US-JennyNeural | 友善温柔 |
| Emma (女声, 轻快) | en-US-EmmaMultilingualNeural | 轻快活泼 |
| Guy (男声, 热情) | en-US-GuyNeural | 热情 |
| Christopher (男声, 权威) | en-US-ChristopherNeural | 权威沉稳 |
| Andrew (男声, 温暖) | en-US-AndrewMultilingualNeural | 温暖 |
| Brian (男声, 随意) | en-US-BrianMultilingualNeural | 随意自然 |

### 语速控制

五档可调，通过 `--rate` 参数实现：

| 档位 | edge-tts 参数 | 场景 |
|:----:|:-------------:|------|
| 0.5x | `--rate=-50%` | 精听每个单词 |
| 0.75x | `--rate=-25%` | 慢速学习 |
| 1.0x | 默认 | 正常语速 |
| 1.25x | `--rate=+25%` | 快速浏览 |
| 1.5x | `--rate=+50%` | 挑战听力 |

### 开头丢字修复

**问题：** edge-playback 流式播放时，Python 进程初始化和网络连接需要时间，导致开头 1-2 秒音频被吞。

**修复：** 改为两步流程——
1. `edge-tts --write-media temp.mp3` — 先生成完整音频文件
2. `Windows Media Player COM` — 后台静默播放，播完自动清理

---

## 界面变更

ReadingPanel 和 ConversationPanel 底部均新增：

```
🔊 TTS  |  Voice: [Aria (女声, 自信) ▼]  |  Speed: [1.0x (正常) ▼]  |  🎤 Record  ...
```

- **🔊 TTS** — 点击切换为 🔇 TTS（关闭语音）
- **Voice 下拉** — 选择发音人
- **Speed 下拉** — 选择语速

---

## 新增依赖

```bash
pip install edge-tts
```

仅需安装一次。其他依赖不变（JDK + Gson + whisper.cpp）。

---

## 运行方式

双击 `run.bat` 或在终端执行：

```bash
cd e:\EN_study_tool\Eng_verbal_practice_tool
D:\JDK\bin\java -XX:+UseSerialGC -Xmx128m -cp "target/classes;target\gson-2.11.0.jar" com.englishspeaker.EnglishSpeakerApp
```

---

## 项目完整依赖

| 组件 | 用途 | 安装方式 |
|------|------|---------|
| JDK ≥ 17 | Java 运行环境 | 手动安装 |
| Maven | 构建工具 | 手动安装 |
| Gson | JSON 解析 | Maven 自动下载 |
| whisper.cpp | 语音识别（STT）| 下载预编译包 |
| **edge-tts** | **语音合成（TTS）** | **pip install** |
