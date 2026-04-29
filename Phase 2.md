# Phase 2：录音输入

## 目标

在 Phase 1 的 Swing GUI 基础上，增加麦克风录音功能。用户可以直接对着麦克风说话代替打字。

---

## 新增文件

```
e:\EN_study_tool\Eng_verbal_practice_tool\
├── src/main/java/com/englishspeaker/service/
│   ├── AudioRecorderService.java    — 录音控制（开始/停止）
│   └── SpeechToTextService.java     — 调用 whisper.cpp 转文字
├── STT/
│   ├── whisper-cli.exe              — whisper.cpp 执行程序
│   ├── ggml-base.bin                — 语音识别模型（base，~141MB）
│   ├── ggml.dll                     — 运行时依赖
│   ├── ggml-base.dll
│   ├── ggml-cpu.dll
│   └── SDL2.dll
```

---

## 工作流程

```
用户点 🎤 Record → AudioRecorder 开始录音（麦克风）
     ↓
用户点 🔴 Stop → 录音结束，保存为 record_temp.wav
     ↓
SpeechToTextService 调用 whisper-cli.exe 转录
     ↓
识别的文字自动填入输入框
     ↓
用户直接点 Evaluate / Send 继续
```

---

## 各模块说明

### AudioRecorderService.java
使用 `javax.sound.sampled`（JDK 内置）从麦克风采集音频：
- `startRecording()` — 在新线程中录音，数据暂存内存
- `stopRecording()` — 停止录音，保存为 16kHz 16-bit 单声道 WAV 文件
- `isRecording()` — 查询当前是否在录音中
- 无第三方依赖

### SpeechToTextService.java
用 `ProcessBuilder` 调用 whisper.cpp 的命令行界面：
- `transcribe(wavFile)` — 执行 `whisper-cli.exe -f file.wav -m model.bin -otxt`
- 等待命令执行完毕，读取生成的 `.txt` 文件得到文字
- 自动清理临时输出文件

---

## 界面变更

### ReadingPanel
- 底部新增"🎤 Record"按钮
- 点击开始录音，按钮变为"🔴 Stop"
- 再次点击停止，自动转录，文字填入输入框

### ConversationPanel
- 输入框旁新增"🎤 Record"按钮
- 同上录音→转录→自动填入输入框
- 录音按钮在对话未开始时自动禁用

---

## 容错设计

- 检测不到麦克风时抛出异常，界面显示提示
- whisper-cli.exe 或模型文件缺失时给出明确错误消息
- 转录过程在后台线程执行（SwingWorker），界面不卡死
- 转录失败时按钮恢复可用，不阻塞后续操作

---

## 运行方式

```bash
# 编译（需指定 Gson 依赖路径）
D:\JDK\bin\javac -cp "c:\Users\21794\.m2\repository\com\google\code\gson\gson\2.11.0\gson-2.11.0.jar" -d target/classes src/main/java/com/englishspeaker/**/*.java

# 运行
D:\JDK\bin\java -cp "target/classes;c:\Users\21794\.m2\repository\com\google\code\gson\gson\2.11.0\gson-2.11.0.jar" com.englishspeaker.EnglishSpeakerApp
```

也可以使用 Maven（如内存不足可参考上面直接编译）：
```bash
D:\apache-maven-3.9.15-bin\apache-maven-3.9.15\bin\mvn clean package
D:\JDK\bin\java -jar target\english-speaker-1.0.jar
```

---

## 依赖

| 组件 | 说明 |
|------|------|
| **whisper.cpp** | 本地语音识别引擎（C++，无 Python 依赖）|
| **ggml-base.bin** | Whisper base 模型（英文，~141MB，~94% 准确率）|
| **javax.sound.sampled** | JDK 内置音频捕获 API |
