# Phase 0：控制台文本原型

## 目标

纯键盘交互的控制台程序，跑通英语口语练习的完整业务流程。不涉及音频和 GUI。

---

## 项目结构

```
e:\EN_study_tool\Eng_verbal_practice_tool\
├── App.java                 — 主入口，菜单循环调度
├── UserManager.java         — 用户注册 / 登录 / 登出
├── ApiClient.java           — 调用 DeepSeek API（HTTP 请求 + JSON 解析）
├── ReadingPractice.java     — 朗读评测：展示文章 → 用户打字 → AI 评分
├── ConversationPractice.java— 情景对话：多轮对话（面试/聊天/求助）
├── texts.txt                — 朗读语料库（5 篇英文短文）
├── config.properties        — API 配置（已加入 .gitignore）
├── config.properties.example— 配置示例（可安全提交）
└── .gitignore               — 忽略 config.properties、users.txt、*.class
```

---

## 各文件职责

### App.java
主程序入口。用 `while` 循环 + `switch` 控制菜单流程：
- 未登录时：Register / Login / Exit
- 已登录后：Reading Test / Conversation Practice / Logout

### UserManager.java
基于文本文件 `users.txt` 的简单用户系统：
- `register()` — 追加写入 `用户名\t密码`
- `login()` — 逐行查找匹配
- `logout()` / `getCurrentUser()` — 会话状态管理

### ApiClient.java
核心网络模块，使用 JDK 内置 `java.net.http.HttpClient`：
- 从 `config.properties` 读取 endpoint、key、model
- 拼接 JSON 请求体，POST 到 DeepSeek API
- 从响应 JSON 中提取 `content` 字段（`indexOf` + `substring` 手动解析）

### ReadingPractice.java
- 从 `texts.txt` 随机加载一篇文章展示
- 用户打字"朗读"
- 调用 AI 评测，返回评分 + 反馈 + 改进建议

### ConversationPractice.java
- 用户选择场景：面试 / 朋友聊天 / 求助
- 进入对话循环，每次输入发送给 AI 并展示回复
- 输入 `exit` 退出

---

## 运行方式

```bash
cd e:\EN_study_tool\Eng_verbal_practice_tool
D:\JDK\bin\javac *.java
D:\JDK\bin\java App
```

需 JDK ≥ 11（`java.net.http.HttpClient` 要求）。

---

## 依赖

- 无第三方库，纯 JDK 自带 API
- 需 DeepSeek API Key（配置于 `config.properties`）
