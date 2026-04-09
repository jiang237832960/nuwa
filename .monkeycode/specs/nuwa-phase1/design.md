# 女娲/Nuwa 阶段1设计文档

## 1. 整体架构

### 1.1 数据流

```
用户输入 → IntentParser → TaskPlanner → Executor → 设备操作
                ↓            ↓            ↓
           WorldState    WorldState   WorldState
```

### 1.2 组件关系

```
MainActivity
    ├── ChatScreen (用户输入)
    ├── TaskExecutionScreen (执行进度)
    ├── SettingsScreen (配置)
    └── NuwaAccessibilityService (无障碍)
            ↓
    AccessibilityBridge (Kotlin)
            ↓
    RustBridge (JNI)
            ↓
    NuwaCore (Rust)
    ├── Agent (意图理解/规划)
    ├── WorldState (状态管理)
    └── Audit (审计日志)
```

## 2. 模块设计

### 2.1 IntentParser (意图理解)

**输入**: 自然语言文本
**输出**: 结构化 Intent

```rust
struct Intent {
    intent_type: IntentType,
    target_app: Option<String>,
    entities: Vec<Entity>,
    confidence: f32,
}
```

**支持的意图类型**:
- SendMessage (发送消息)
- SearchContact (搜索联系人)
- Navigate (导航)
- OpenFile (打开文件)
- SaveImage (保存图片)
- QueryBill (查询账单)

### 2.2 TaskPlanner (任务规划)

**职责**: 将意图转换为可执行步骤

```rust
struct Task {
    id: String,
    steps: Vec<Step>,
    status: TaskStatus,
}
```

**步骤类型**:
- OpenApp
- Click
- Input
- Swipe
- Wait
- Scroll

### 2.3 Executor (执行器)

**职责**: 执行步骤并处理反馈

```rust
struct Executor {
    accessibility: AccessibilityBridge,
}
```

### 2.4 WorldState (世界状态)

```rust
struct WorldState {
    foreground_app: String,
    ui_tree: Option<UiTree>,
    device_resources: DeviceResources,
    task_state: Option<TaskState>,
}
```

### 2.5 Audit (审计日志)

```rust
struct AuditRecord {
    timestamp: u64,
    event_type: EventType,
    details: String,
}
```

## 3. Android UI 设计

### 3.1 主界面布局

```
┌─────────────────────────────┐
│ 女娲                         │
├─────────────────────────────┤
│                             │
│  [对话历史区域]              │
│                             │
├─────────────────────────────┤
│ [输入框]              [发送] │
├─────────────────────────────┤
│ 服务状态: 已连接             │
└─────────────────────────────┘
```

### 3.2 任务执行界面

```
┌─────────────────────────────┐
│ 任务执行中                  │
├─────────────────────────────┤
│ ● 打开微信          [完成]  │
│ ○ 搜索联系人        [进行中]│
│ ○ 进入聊天          [等待] │
│ ○ 发送消息          [等待] │
├─────────────────────────────┤
│ [当前界面截图]              │
├─────────────────────────────┤
│ [取消任务]                  │
└─────────────────────────────┘
```

## 4. 中国应用优先矩阵

### 4.1 一级优先 (核心)

| 应用 | 包名 | 主要操作 |
|------|------|----------|
| 微信 | com.tencent.mm | 发消息、朋友圈 |
| 高德地图 | com.autonavi.minimap | 导航、搜索 |
| WPS | cn.wps.moffice_eng | 打开文档 |
| 支付宝 | com.eg.android.AlipayGphone | 账单、付款 |

### 4.2 应用适配配置

```kotlin
object AppAdapter {
    val apps = mapOf(
        "com.tencent.mm" to AppConfig(
            name = "微信",
            searchBoxId = "com.tencent.mm:id/con",
            sendButtonText = "发送"
        ),
        "com.autonavi.minimap" to AppConfig(...),
        "cn.wps.moffice_eng" to AppConfig(...),
        "com.eg.android.AlipayGphone" to AppConfig(...)
    )
}
```

## 5. 技术实现

### 5.1 Rust 核心

```toml
[dependencies]
nuwa-agent = { path = "../nuwa-agent" }
nuwa-world = { path = "../nuwa-world" }
nuwa-audit = { path = "../nuwa-audit" }
```

### 5.2 Android 桥接

```kotlin
class NuwaCoreBridge {
    external fun parseIntent(text: String): String
    external fun planTask(intentJson: String): String
    external fun executeStep(stepJson: String): String
    external fun getWorldState(): String
    external fun getAuditLog(): String
}
```

## 6. 验证方案

### 6.1 单元测试
- IntentParser 测试
- TaskPlanner 测试
- Executor mock 测试

### 6.2 集成测试
- 微信发消息流程
- 高德导航流程
- WPS 打开文件流程

### 6.3 人工验收
- 服务连接稳定性
- 任务完成率
- 错误处理正确性
