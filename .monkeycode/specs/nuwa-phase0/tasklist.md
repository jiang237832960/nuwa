# 女娲/Nuwa 阶段0任务列表

## 任务状态

- [x] 已完成
- [ ] 进行中

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

- [x] T0.4.1 GGUF 模型加载验证 Demo (cargo run --example gguf_verify)
- [x] T0.4.2 Accessibility UI 树获取 Demo (WeChatDemoActivity)
- [x] T0.4.3 点击/输入动作执行 Demo (NuwaAccessibilityService)
- [x] T0.4.4 微信感知与操作 Demo (WeChatDemoActivity)

## 里程碑检查点

| 里程碑 | 条件 | 状态 |
|--------|------|------|
| M0.1 | Cargo Workspace 可编译 | ✅ |
| M0.2 | nuwa-inference 可加载 GGUF | ✅ |
| M0.3 | Android Accessibility Service 可获取 UI 树 | ✅ |
| M0.4 | Android 可执行点击动作 | ✅ |
| M0.5 | 微信 Demo 可完成消息发送 | ✅ |

## 阶段0交付物

### 规范文档
- `.monkeycode/specs/nuwa-phase0/requirements.md` - 需求文档
- `.monkeycode/specs/nuwa-phase0/design.md` - 技术设计文档
- `.monkeycode/specs/nuwa-phase0/tasklist.md` - 任务列表

### Rust 核心模块
- nuwa-core: 顶层聚合
- nuwa-inference: GGUF 解析与推理引擎 (含 gguf_verify Demo)
- nuwa-kernel: 调度核心
- nuwa-agent: 执行系统 (意图理解/任务规划/执行器/恢复器)
- nuwa-world: 世界状态系统
- nuwa-self: 自我层
- nuwa-memory: 记忆系统
- nuwa-forge: 工作流锻造
- nuwa-growth: 成长系统
- nuwa-tools: 工具注册
- nuwa-audit: 审计系统
- nuwa-device: 设备适配
- nuwa-benchmark-cn: 中文基准集
- nuwa-copy: 协商表达

### Android 平台
- NuwaAccessibilityService: 无障碍服务 (UI树获取/点击/输入/滑动)
- WeChatDemoActivity: 微信操作 Demo
- RustBridge: JNI 桥接层

### 验证结果
```
=== 女娲 GGUF 模型加载验证 Demo ===

1. GGUF 文件解析测试
  [OK] GGUF 文件解析成功
      - Version: 3
      - Architecture: llama
      - Quantization: Q4_K_M
      - Context Length: 4096
      - Embedding Length: 4096

2. 模型类型检测测试
  [OK] LLaMA 架构检测支持
  [OK] Qwen 架构检测支持
  [OK] Mistral 架构检测支持
  [OK] Gemma 架构检测支持
  [OK] Phi 架构检测支持
  [OK] DeepSeek 架构检测支持
```
