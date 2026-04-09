use serde::{Deserialize, Serialize};
use parking_lot::RwLock;
use std::sync::Arc;

pub struct Device {
    info: DeviceInfo,
    capability_map: CapabilityMap,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DeviceInfo {
    pub brand: Brand,
    pub model: String,
    pub rom_type: ROMType,
    pub os_version: String,
    pub sdk_version: u32,
    pub cpu_cores: usize,
    pub memory_total: u64,
    pub storage_total: u64,
    pub has_npu: bool,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum Brand {
    Huawei,
    Honor,
    Xiaomi,
    Oppo,
    Vivo,
    OnePlus,
    Realme,
    Samsung,
    Other,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum ROMType {
    Stock,
    MIUI,
    ColorOS,
    OriginOS,
    HarmonyOS,
    MagicUI,
    OneUI,
    Other,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CapabilityMap {
    pub inference_speed: InferenceCapability,
    pub automation_level: AutomationLevel,
    pub background_running: BackgroundCapability,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum InferenceCapability {
    Slow,
    Normal,
    Fast,
    Turbo,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum AutomationLevel {
    Basic,
    Intermediate,
    Advanced,
    Full,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum BackgroundCapability {
    Restricted,
    Limited,
    Normal,
    Unrestricted,
}

impl Device {
    pub fn new() -> Self {
        Self {
            info: DeviceInfo {
                brand: Brand::Other,
                model: String::new(),
                rom_type: ROMType::Stock,
                os_version: String::from("Unknown"),
                sdk_version: 0,
                cpu_cores: 4,
                memory_total: 4 * 1024 * 1024 * 1024,
                storage_total: 64 * 1024 * 1024 * 1024,
                has_npu: false,
            },
            capability_map: CapabilityMap {
                inference_speed: InferenceCapability::Normal,
                automation_level: AutomationLevel::Basic,
                background_running: BackgroundCapability::Limited,
            },
        }
    }

    pub fn detect_rom_type(&mut self) -> ROMType {
        #[cfg(target_os = "android")]
        {
            let build_props = std::fs::read_to_string("/system/build.prop").unwrap_or_default();
            if build_props.contains("ro.build.version.emui") {
                return ROMType::MagicUI;
            }
            if build_props.contains("ro.miui.ui.version") {
                return ROMType::MIUI;
            }
            if build_props.contains("ro.build.version.opporom") {
                return ROMType::ColorOS;
            }
            if build_props.contains("ro.vivo.os.version") {
                return ROMType::OriginOS;
            }
            if build_props.contains("ro.build.version.harmonyos") {
                return ROMType::HarmonyOS;
            }
        }
        ROMType::Stock
    }
}

impl Default for Device {
    fn default() -> Self {
        Self::new()
    }
}
