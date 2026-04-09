# 用户指令记忆

本文件记录了用户的指令、偏好和教导，用于在未来的交互中提供参考。

## 格式

### 用户指令条目
用户指令条目应遵循以下格式：

[用户指令摘要]
- Date: [YYYY-MM-DD]
- Context: [提及的场景或时间]
- Instructions:
  - [用户教导或指示的内容，逐行描述]

### 项目知识条目
Agent 在任务执行过程中发现的条目应遵循以下格式：

[项目知识摘要]
- Date: [YYYY-MM-DD]
- Context: Agent 在执行 [具体任务描述] 时发现
- Category: [代码结构|代码模式|代码生成|构建方法|测试方法|依赖关系|环境配置]
- Instructions:
  - [具体的知识点，逐行描述]

## 去重策略
- 添加新条目前，检查是否存在相似或相同的指令
- 若发现重复，跳过新条目或与已有条目合并
- 合并时，更新上下文或日期信息
- 这有助于避免冗余条目，保持记忆文件整洁

## 条目

[项目知识摘要]
- Date: 2026-04-09
- Context: Agent 在分析女娲/Nuwa项目方案时发现
- Category: 代码结构
- Instructions:
  - 项目采用 Cargo Workspace 结构，crates/ 目录下包含多个子 crate
  - 核心层使用 Rust，Android 应用层使用 Kotlin，Windows GUI 可选 Tauri + React
  - 顶层聚合 crate 为 nuwa-core，device 适配层为 nuwa-device
  - 五个横向系统：World State、Growth System、Audit & Versioning、Device Adaptation、Negotiation Copy
  - 五层生命架构：Body(身体层) → Perception(感知层) → Action(行动层) → Cognition(认知层) → Self(自我层)

[项目知识摘要]
- Date: 2026-04-09
- Context: Agent 在创建 nuwa 项目结构时发现
- Category: 构建方法
- Instructions:
  - Android 项目使用 Gradle 构建系统，Kotlin 语言
  - Rust 项目使用 Cargo 管理
  - Tauri 2 + React 用于 Windows GUI
  - 首选 GGUF 模型格式，首选 llama.cpp 作为推理引擎
  - 使用 tokio 作为 Rust async 运行时

[项目知识摘要]
- Date: 2026-04-09
- Context: Agent 在分析阶段0目标时发现
- Category: 测试方法
- Instructions:
  - 阶段0需要验证：Android 权限与能力 Demo、llama.cpp 安卓加载、Accessibility 控制闭环、GGUF 模型导入解析、至少1个中国高频应用验证
  - 交付标准：跑通模型加载、无障碍感知、基础控制、中国高频应用验证
