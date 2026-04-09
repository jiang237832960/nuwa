# 女娲/Nuwa 阶段0设计文档

## 1. 架构设计

### 1.1 整体架构

```
nuwa/
├── Cargo.toml                    # Workspace 根配置
├── crates/
│   ├── nuwa-core/               # 顶层聚合 crate
│   ├── nuwa-inference/          # 模型推理层 (llama.cpp 封装)
│   ├── nuwa-kernel/             # 不可变边界与调度核心
│   ├── nuwa-agent/              # 执行系统 (规划器、执行器)
│   ├── nuwa-world/              # 世界状态系统
│   ├── nuwa-self/               # 自我层
│   ├── nuwa-memory/             # 记忆系统
│   ├── nuwa-forge/              # Tool Forge
│   ├── nuwa-growth/             # 成长系统
│   ├── nuwa-tools/              # 工具注册与执行
│   ├── nuwa-audit/              # 审计与版本
│   ├── nuwa-device/             # 设备适配层
│   ├── nuwa-benchmark-cn/        # 中文任务基准集
│   └── nuwa-copy/                # 协商与产品文案层
├── platform/
│   ├── android-bridge/          # Android 平台桥接
│   └── windows-bridge/           # Windows 平台桥接
└── apps/
    ├── android/                 # Android App (Kotlin)
    └── windows/                 # Windows App (Tauri)
```

### 1.2 五层生命架构

| 层级 | 职责 | 关键模块 |
|------|------|----------|
| Body | 设备资源画像与约束 | nuwa-device |
| Perception | 看见当前环境 | nuwa-world, android-bridge |
| Action | 执行设备操作 | nuwa-tools, android-bridge |
| Cognition | 理解、规划、策略优化 | nuwa-agent, nuwa-inference |
| Self | 资源自知、能力认知、成长协商 | nuwa-self, nuwa-growth |

### 1.3 五个横向系统

- **World State**: 统一环境认知输入
- **Growth System**: 经验沉淀与能力增长
- **Audit & Versioning**: 全链路可追溯
- **Device Adaptation**: 设备能力适配
- **Negotiation Copy**: 用户友好协商表达

## 2. 核心模块设计

### 2.1 nuwa-inference (推理引擎层)

**职责**: 封装 llama.cpp，提供模型加载与推理能力

**接口设计**:

```rust
pub trait InferenceEngine {
    fn load_model(&mut self, path: &Path) -> Result<ModelHandle>;
    fn generate(&self, handle: ModelHandle, prompt: &str) -> Result<String>;
    fn get_token_count(&self, text: &str) -> usize;
}
```

**依赖**:
- llama.cpp (via git submodule or crate)
- serde, serde_json
- tokio

### 2.2 nuwa-kernel (内核调度层)

**职责**: 不可变边界，负责任务调度与生命周期管理

**核心组件**:
- TaskScheduler: 任务队列与调度
- ResourceManager: 资源分配与监控
- LifecycleManager: 应用状态管理

### 2.3 nuwa-agent (执行系统)

**职责**: 把用户目标转化为设备行为

**子模块**:
- IntentParser: 意图理解
- TaskPlanner: 任务规划
- Executor: 步骤执行
- Recovery: 错误恢复

### 2.4 nuwa-world (世界状态)

**职责**: 统一环境认知输入

**WorldState 结构**:
```rust
struct WorldState {
    foreground_app: String,
    ui_tree: UiTree,
    device_resources: DeviceResources,
    task_state: TaskState,
    user_context: UserContext,
    restrictions: Restrictions,
    adaptation_strategy: AdaptationStrategy,
}
```

### 2.5 android-bridge (Android 平台桥接)

**职责**: Android 平台特定实现

**感知能力**:
- AccessibilityService: UI 树获取
- MediaProjection: 截图
- NotificationListener: 通知监听
- UsageStats: 使用统计

**执行能力**:
- AccessibilityNodeInfo: 点击、输入、滚动
- Intent: 应用跳转
- ContentProvider: 数据访问

## 3. 数据流设计

### 3.1 任务执行流程

```
用户输入 → IntentParser → TaskPlanner → Executor
                                          ↓
                                   WorldState 更新
                                          ↓
                                   动作反馈 → 状态判断
                                          ↓
                              成功 → 记录经验 | 失败 → Recovery
```

### 3.2 模型推理流程

```
文本输入 → Tokenizer → 模型推理 → Decoder → 文本输出
```

## 4. 阶段0技术验证点

### 4.1 GGUF 解析验证

```rust
struct GGUFMeta {
    version: u32,
    arch: String,
    quantization: String,
    context_length: u32,
    embedding_length: u32,
}
```

### 4.2 Accessibility 闭环验证

```kotlin
// 伪代码 - Android 侧
class AccessibilityBridge {
    fun getUiTree(): UiTree
    fun performClick(node: AccessibilityNodeInfo): Boolean
    fun performInput(node: AccessibilityNodeInfo, text: String): Boolean
}
```

## 5. 依赖版本

| 组件 | 版本 | 说明 |
|------|------|------|
| rustc | stable | 1.75+ |
| tokio | 1.x | async runtime |
| llama.cpp | main | via ggmlrs crate 或 submodule |
| kotlin | 1.9.x | |
| gradle | 8.x | |
| compose | 1.5.x | BOM 2024.01.00 |
| targetSdk | 34 | Android 14 |
| minSdk | 26 | Android 8.0 |
