use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct NegotiationMessage {
    pub title: String,
    pub user_benefit: String,
    pub current_limit: String,
    pub options: Vec<NegotiationOption>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct NegotiationOption {
    pub label: String,
    pub action: String,
}

pub struct NegotiationCopy;

impl NegotiationCopy {
    pub fn new() -> Self {
        Self
    }

    pub fn format_message(&self, message: &str) -> NegotiationMessage {
        NegotiationMessage {
            title: "我想变得更好".to_string(),
            user_benefit: format!("这样做可以让我更好地帮助你: {}", message),
            current_limit: "当前设备资源有限".to_string(),
            options: vec![
                NegotiationOption {
                    label: "好的，今晚处理".to_string(),
                    action: "approve".to_string(),
                },
                NegotiationOption {
                    label: "不用了".to_string(),
                    action: "decline".to_string(),
                },
            ],
        }
    }
}

impl Default for NegotiationCopy {
    fn default() -> Self {
        Self::new()
    }
}
