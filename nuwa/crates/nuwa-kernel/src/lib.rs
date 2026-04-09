use thiserror::Error;
use serde::{Deserialize, Serialize};
use std::sync::Arc;
use parking_lot::RwLock;

#[derive(Error, Debug)]
pub enum KernelError {
    #[error("Task scheduling failed: {0}")]
    ScheduleError(String),
    #[error("Resource not available: {0}")]
    ResourceError(String),
    #[error("Lifecycle error: {0}")]
    LifecycleError(String),
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ResourceInfo {
    pub cpu_cores: usize,
    pub memory_total: u64,
    pub memory_available: u64,
    pub battery_level: Option<f32>,
    pub is_charging: bool,
    pub temperature: Option<f32>,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum AppState {
    Foreground,
    Background,
    Service,
}

pub struct NuwaKernel {
    resource_info: Arc<RwLock<ResourceInfo>>,
    app_state: Arc<RwLock<AppState>>,
}

impl NuwaKernel {
    pub fn new() -> Self {
        Self {
            resource_info: Arc::new(RwLock::new(ResourceInfo {
                cpu_cores: 1,
                memory_total: 0,
                memory_available: 0,
                battery_level: None,
                is_charging: false,
                temperature: None,
            })),
            app_state: Arc::new(RwLock::new(AppState::Background)),
        }
    }

    pub fn update_resources(&self, info: ResourceInfo) {
        *self.resource_info.write() = info;
    }

    pub fn get_resources(&self) -> ResourceInfo {
        self.resource_info.read().clone()
    }

    pub fn set_app_state(&self, state: AppState) {
        *self.app_state.write() = state;
    }

    pub fn get_app_state(&self) -> AppState {
        *self.app_state.read()
    }
}
