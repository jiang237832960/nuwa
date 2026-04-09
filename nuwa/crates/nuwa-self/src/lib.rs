use serde::{Deserialize, Serialize};
use parking_lot::RwLock;
use std::sync::Arc;

pub use nuwa_device::{Device, DeviceInfo, Brand, ROMType};
pub use nuwa_growth::{Growth, GrowthBudget, BudgetInfo};
pub use nuwa_copy::{NegotiationCopy, NegotiationMessage};

pub struct SelfLayer {
    device: Arc<RwLock<Device>>,
    growth: Arc<RwLock<Growth>>,
    copy: Arc<RwLock<NegotiationCopy>>,
}

impl SelfLayer {
    pub fn new() -> Self {
        Self {
            device: Arc::new(RwLock::new(Device::new())),
            growth: Arc::new(RwLock::new(Growth::new())),
            copy: Arc::new(RwLock::new(NegotiationCopy::new())),
        }
    }

    pub fn get_device_info(&self) -> Device {
        self.device.read().clone()
    }

    pub fn update_device_info(&self, info: Device) {
        *self.device.write() = info;
    }

    pub fn get_budget_info(&self) -> BudgetInfo {
        self.growth.read().get_budget_info()
    }

    pub fn request_negotiation(&self, message: &str) -> NegotiationMessage {
        self.copy.read().format_message(message)
    }
}

impl Default for SelfLayer {
    fn default() -> Self {
        Self::new()
    }
}
