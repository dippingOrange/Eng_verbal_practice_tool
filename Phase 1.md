# Phase 1：Swing GUI

## 目标

用图形界面代替控制台，保留 Phase 0 全部功能，提升用户体验。引入 Maven 构建工具和 Gson JSON 库。

---

## 新增/变更

| 项目 | 说明 |
|------|------|
| **Maven** | 构建工具，自动管理依赖 |
| **Gson** | Google JSON 解析库（替代 Phase 0 的手动字符串解析）|
| **Swing** | JDK 内置 GUI 框架（JFrame、JPanel、CardLayout）|
| **package** | Java 包机制，分层组织代码 |

---

## 项目结构

```
e:\EN_study_tool\Eng_verbal_practice_tool\
├── pom.xml                          — Maven 构建配置
├── src/main/java/com/englishspeaker/
│   ├── EnglishSpeakerApp.java       — 主窗口（JFrame + CardLayout）
│   ├── gui/
│   │   ├── LoginPanel.java          — 登录/注册界面
│   │   ├── HomePanel.java           — 主菜单界面
│   │   ├── ReadingPanel.java        — 朗读测试界面（含 SwingWorker）
│   │   └── ConversationPanel.java   — 情景对话界面
│   ├── service/
│   │   ├── UserService.java         — 用户管理（USerManager 升级版）
│   │   ├── AiService.java           — DeepSeek API 调用（Gson 版）
│   │   ├── ReadingService.java      — 朗读评测服务
│   │   └── ConversationService.java — 对话服务
│   └── model/
│       ├── User.java                — 用户模型
│       ├── Scenario.java            — 场景枚举（面试/聊天/求助）
│       └── ReadingResult.java       — 评测结果模型
├── config.properties                — API 配置（已加入 .gitignore）
├── config.properties.example        — 配置示例
├── texts.txt                        — 朗读语料
└── .gitignore                       — 忽略敏感/生成文件
```

---

## 各层职责

### model 层
纯数据类，不包含业务逻辑。

- **User.java** — 用户名 + 密码 POJO
- **Scenario.java** — 三个对话场景的枚举，每个场景携带中文标签和英文 Prompt
- **ReadingResult.java** — 评测结果（score / feedback / tips）

### service 层
封装业务逻辑，与 GUI 解耦。

- **UserService.java** — 从 Phase 0 的 UserManager 升级而来，读写 users.txt
- **AiService.java** — 从 Phase 0 的 ApiClient 升级，改用 Gson 构建请求和解析响应，更稳定
- **ReadingService.java** — 随机选文章 + 调 AI 评测 + 用 Gson 解析返回的 JSON 结果
- **ConversationService.java** — 管理对话场景状态，发送消息

### gui 层
Swing 界面组件。

- **LoginPanel.java** — 用户名/密码输入框，注册和登录按钮
- **HomePanel.java** — 功能菜单按钮（朗读测试 / 情景对话 / 登出）
- **ReadingPanel.java** — 展示文章 → 用户输入 → 显示评分结果，使用 SwingWorker 避免界面卡死
- **ConversationPanel.java** — 场景选择下拉框 → 对话聊天界面，实时显示对话历史

### EnglishSpeakerApp.java
主窗口，使用 `CardLayout` 切换四个面板，管理应用生命周期。

---

## 关键改进（相比 Phase 0）

1. **JSON 解析** — 用 Gson 库替代手写字符串解析，避免转义字符导致的解析错误
2. **异步操作** — 使用 `SwingWorker` 在后台线程调用 API，界面不卡死
3. **包结构** — 按 model/service/gui 分层，代码更清晰
4. **错误处理** — API 调用异常时弹出提示而非直接崩溃

---

## 运行方式

```bash
# 编译打包
D:\apache-maven-3.9.15-bin\apache-maven-3.9.15\bin\mvn clean package

# 运行
D:\JDK\bin\java -jar target\english-speaker-1.0.jar
```

需 JDK ≥ 17（使用了 pattern matching instanceof、switch 表达式等语言特性）。

---

## 依赖

- **Gson 2.11.0** — JSON 序列化/反序列化（Maven 自动下载）
- 其余全部使用 JDK 内置 API（Swing、HttpClient、java.time 等）
