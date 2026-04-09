use serde::{Deserialize, Serialize};
use parking_lot::RwLock;
use std::sync::Arc;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UiNode {
    pub id: String,
    pub class_name: String,
    pub text: Option<String>,
    pub content_desc: Option<String>,
    pub bounds: Rect,
    pub children: Vec<UiNode>,
    pub clickable: bool,
    pub focusable: bool,
}

#[derive(Debug, Clone, Copy, Serialize, Deserialize)]
pub struct Rect {
    pub left: i32,
    pub top: i32,
    pub right: i32,
    pub bottom: i32,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UiTree {
    pub root: UiNode,
    pub package_name: String,
    pub activity_name: String,
    pub timestamp: u64,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DeviceResources {
    pub cpu_usage: f32,
    pub memory_used: u64,
    pub memory_total: u64,
    pub battery_level: f32,
    pub is_charging: bool,
    pub screen_on: bool,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum TaskStatus {
    Idle,
    Running,
    Paused,
    Completed,
    Failed,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct TaskState {
    pub task_id: String,
    pub status: TaskStatus,
    pub current_step: usize,
    pub total_steps: usize,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UserContext {
    pub user_id: Option<String>,
    pub current_app: String,
    pub recent_apps: Vec<String>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Restrictions {
    pub battery_low: bool,
    pub data_saver_on: bool,
    pub background_restricted: bool,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AdaptationStrategy {
    pub rom_type: RomType,
    pub brand: String,
    pub model: String,
    pub capability_level: CapabilityLevel,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum RomType {
    Stock,
    MIUI,
    ColorOS,
    OriginOS,
    HarmonyOS,
    MagicUI,
    OneUI,
    Other,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum CapabilityLevel {
    Low,
    Medium,
    High,
    Flagship,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct WorldState {
    pub foreground_app: String,
    pub ui_tree: Option<UiTree>,
    pub device_resources: DeviceResources,
    pub task_state: Option<TaskState>,
    pub user_context: UserContext,
    pub restrictions: Restrictions,
    pub adaptation_strategy: AdaptationStrategy,
}

impl WorldState {
    pub fn new() -> Self {
        Self {
            foreground_app: String::new(),
            ui_tree: None,
            device_resources: DeviceResources {
                cpu_usage: 0.0,
                memory_used: 0,
                memory_total: 0,
                battery_level: 1.0,
                is_charging: false,
                screen_on: true,
            },
            task_state: None,
            user_context: UserContext {
                user_id: None,
                current_app: String::new(),
                recent_apps: Vec::new(),
            },
            restrictions: Restrictions {
                battery_low: false,
                data_saver_on: false,
                background_restricted: false,
            },
            adaptation_strategy: AdaptationStrategy {
                rom_type: RomType::Stock,
                brand: String::new(),
                model: String::new(),
                capability_level: CapabilityLevel::Medium,
            },
        }
    }
}

pub struct WorldStateManager {
    state: Arc<RwLock<WorldState>>,
}

impl WorldStateManager {
    pub fn new() -> Self {
        Self {
            state: Arc::new(RwLock::new(WorldState::new())),
        }
    }

    pub fn update(&self, new_state: WorldState) {
        *self.state.write() = new_state;
    }

    pub fn get(&self) -> WorldState {
        self.state.read().clone()
    }

    pub fn update_ui_tree(&self, tree: UiTree) {
        self.state.write().ui_tree = Some(tree);
    }

    pub fn update_resources(&self, resources: DeviceResources) {
        self.state.write().device_resources = resources;
    }
}
