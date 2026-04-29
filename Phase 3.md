# Phase 3：语音回复（TTS）

## 目标

AI 的回复以语音形式朗读出来，让口语练习更有对话临场感。

---

## 新增文件

```
src/main/java/com/englishspeaker/service/
└── TextToSpeechService.java      — 文字转语音（调用 Windows 内置语音引擎）
```

---

## 实现原理

利用 **Windows 内置的 System.Speech API**，无需安装任何第三方工具。

```
AI 返回文字 → TextToSpeechService → PowerShell 调用 Windows SAPI → 扬声器朗读
```

Java 通过 `ProcessBuilder` 执行 PowerShell 命令：
```powershell
Add-Type -AssemblyName System.Speech
$speak = New-Object System.Speech.Synthesis.SpeechSynthesizer
$speak.Speak('要朗读的文字')
```

---

## TextToSpeechService.java

| 方法 | 说明 |
|------|------|
| `speak(text)` | 同步朗读（阻塞直到读完）|
| `speakAsync(text)` | 异步朗读（新线程，不阻塞 UI）|
| `setEnabled(boolean)` | 开关 TTS |
| `isEnabled()` | 查询 TTS 状态 |

限制：单次朗读不超过 500 字符，避免朗读过长内容。

---

## 界面变更

### ReadingPanel
- 新增 **🔊 TTS** 按钮，点击切换为 **🔇 TTS**（关闭语音）
- 评测结果返回后自动朗读评分和反馈

### ConversationPanel
- 底部新增 **🔊 TTS** 按钮
- AI 每次回复后自动朗读

---

## 运行方式

双击 `run.bat`，或命令行：

```bash
cd e:\EN_study_tool\Eng_verbal_practice_tool
D:\JDK\bin\java -XX:+UseSerialGC -Xmx256m -cp "target/classes;target\gson-2.11.0.jar" com.englishspeaker.EnglishSpeakerApp
```

编译（修改代码后需要）：
```bash
D:\JDK\bin\javac -cp "target\gson-2.11.0.jar" -d target/classes src/main/java/com/englishspeaker/**/*.java
```

---

## 依赖

- **无第三方依赖** — 使用 Windows 内置 `System.Speech`（.NET 组件）
- 仅限 Windows 系统（依赖 SAPI）

---

## 完整交互链路

```
用户说话 → 录音（Phase 2）→ whisper.cpp 转文字 → DeepSeek API → AI 回复文字
                                                                       ↓
                                                                  TTS 语音朗读（Phase 3）
                                                                       ↓
                                                                 同时在界面显示文字
```
