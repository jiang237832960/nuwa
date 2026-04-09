# 女娲/Nuwa 阶段0任务列表

## 任务状态

- [ ] 待开始
- [x] 已完成
- 进行中

## 任务清单

### T0.1 项目骨架搭建

- [x] T0.1.1 创建 Cargo Workspace 根配置
- [x] T0.1.2 创建所有 crate 目录结构
- [x] T0.1.3 创建 Android 项目目录结构
- [x] T0.1.4 创建规范文档

### T0.2 Rust 核心层搭建

- [x] T0.2.1 实现 nuwa-core 顶层聚合
- [x] T0.2.2 实现 nuwa-inference GGUF 解析与 llama.cpp 封装
- [x] T0.2.3 实现 nuwa-kernel 调度核心
- [x] T0.2.4 实现 nuwa-world 世界状态系统
- [x] T0.2.5 实现 nuwa-agent 执行系统骨架

### T0.3 Android 平台层搭建

- [x] T0.3.1 创建 Android Gradle 项目
- [x] T0.3.2 实现 Accessibility Service
- [ ] T0.3.3 实现 MediaProjection 截图服务
- [x] T0.3.4 实现 android-bridge Rust 绑定
- [x] T0.3.5 实现 Kotlin 桥接层

### T0.4 原型验证 Demo

- [ ] T0.4.1 GGUF 模型加载验证 Demo
- [ ] T0.4.2 Accessibility UI 树获取 Demo
- [ ] T0.4.3 点击/输入动作执行 Demo
- [ ] T0.4.4 微信感知与操作 Demo

## 执行顺序

1. **T0.1** (已完成) - 项目骨架
2. **T0.2** - Rust 核心层
3. **T0.3** - Android 平台层
4. **T0.4** - 原型验证 Demo

## 里程碑检查点

| 里程碑 | 条件 |
|--------|------|
| M0.1 | Cargo Workspace 可编译 |
| M0.2 | nuwa-inference 可加载 GGUF |
| M0.3 | Android Accessibility Service 可获取 UI 树 |
| M0.4 | Android 可执行点击动作 |
| M0.5 | 微信 Demo 可完成消息发送 |
