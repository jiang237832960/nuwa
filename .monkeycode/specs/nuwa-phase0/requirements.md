# 女娲/Nuwa 阶段0需求文档

## 1. 需求概述

阶段0目标是冻结架构原则与技术边界，验证最关键技术链路。

## 2. 阶段目标

### 2.1 最终总体设计文档
- 确认五层生命架构边界定义
- 确认五个横向系统职责
- 确认 Rust + Kotlin + TypeScript 技术栈
- 确认 GGUF + llama.cpp 为首选推理方案

### 2.2 Android 权限与能力验证 Demo
- 验证无障碍服务(Accessibility Service)可用性
- 验证截图(MediaProjection)可用性
- 验证通知监听(NotificationListener)可用性
- 验证前台服务(Foreground Service)可用性

### 2.3 llama.cpp 安卓加载验证
- 在 Android 环境成功编译 llama.cpp
- 通过 JNI/FFI 加载 llama.cpp
- 验证 GGUF 模型加载功能
- 验证基础推理功能

### 2.4 Accessibility 控制闭环 Demo
- 获取当前界面 UI 树
- 通过 AccessibilityNodeInfo 执行点击
- 通过 AccessibilityNodeInfo 执行输入
- 验证动作反馈机制

### 2.5 GGUF 模型导入与解析 Demo
- 实现 GGUF 文件解析
- 提取模型元数据(tokenizer info)
- 验证模型架构识别(Qwen/LLaMA/Mistral/Gemma/Phi/DeepSeek)

### 2.6 中国高频应用验证
- 选择微信作为首个验证应用
- 验证微信消息列表感知
- 验证微信聊天界面感知
- 验证发送消息动作执行

## 3. 验收标准

| 验证项 | 标准 |
|--------|------|
| 模型加载 | 能在 Android 上加载 GGUF 并完成推理 |
| 无障碍感知 | 能获取界面 UI 树结构 |
| 动作执行 | 能执行点击、输入并获得反馈 |
| 中国应用 | 能在微信完成基础消息操作 |

## 4. 技术约束

- Android minSdkVersion: 26 (Android 8.0)
- targetSdkVersion: 34 (Android 14)
- Rust: stable edition 2024
- Kotlin: 1.9.x
- llama.cpp: latest stable
