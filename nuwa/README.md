# 女娲 / Nuwa

端侧自主智能体系统，以中国主流 Android 设备为第一落地平台。

## 项目目标

女娲是一个长期运行在用户设备中的本地智能执行体，能够：
- 理解用户意图
- 感知当前设备环境
- 规划并执行具体任务
- 从经验中持续学习成长
- 在资源不足时主动与用户协商

## 核心原则

- **模型完全解耦**: 不绑定主模型，支持 GGUF 格式
- **端侧优先**: 推理、记忆、感知、执行默认本地完成
- **中文与本地生态优先**: 以中国主流应用生态为优化对象
- **用户主权**: 成长请求、资源请求由用户决定
- **结果可追溯**: 全链路记录、可解释、可回放

## 架构

### 五层生命架构

| 层级 | 职责 |
|------|------|
| Body | 设备资源画像与存在约束 |
| Perception | 看见当前环境 |
| Action | 执行设备操作 |
| Cognition | 理解、规划、恢复、总结 |
| Self | 资源自知、能力认知、成长协商 |

### 项目结构

```
nuwa/
├── crates/                    # Rust 核心模块
│   ├── nuwa-core/            # 顶层聚合
│   ├── nuwa-agent/           # 执行系统
│   ├── nuwa-world/           # 世界状态
│   ├── nuwa-self/            # 自我层
│   ├── nuwa-memory/          # 记忆系统
│   ├── nuwa-forge/           # Tool Forge
│   ├── nuwa-growth/          # 成长系统
│   ├── nuwa-inference/       # 推理引擎
│   ├── nuwa-tools/           # 工具注册
│   ├── nuwa-audit/           # 审计系统
│   ├── nuwa-kernel/          # 内核调度
│   ├── nuwa-device/          # 设备适配
│   ├── nuwa-benchmark-cn/     # 中文基准集
│   └── nuwa-copy/            # 协商表达
├── platform/
│   ├── android-bridge/        # Android 桥接
│   └── windows-bridge/        # Windows 桥接
└── apps/
    ├── android/              # Android App
    └── windows/              # Windows App
```

## 技术栈

- **核心语言**: Rust + Kotlin
- **推理引擎**: llama.cpp (GGUF 优先)
- **Android UI**: Jetpack Compose + Material 3
- **Windows GUI**: Tauri 2 + React

## 阶段0里程碑

- [x] Cargo Workspace 项目骨架
- [x] Rust crates 子模块
- [x] nuwa-inference GGUF 解析
- [x] nuwa-world 世界状态系统
- [x] nuwa-agent 执行系统骨架
- [x] Android Accessibility Service
- [x] Android 平台桥接

## 快速开始

### 编译 Rust 核心

```bash
cargo build --release
```

### 构建 Android App

```bash
cd apps/android
./gradlew assembleDebug
```

## 许可证

MIT OR Apache-2.0
