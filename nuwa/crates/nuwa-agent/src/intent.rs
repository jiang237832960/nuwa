use serde::{Deserialize, Serialize};
use thiserror::Error;

#[derive(Error, Debug)]
pub enum IntentError {
    #[error("Failed to parse intent: {0}")]
    ParseError(String),
    #[error("Ambiguous intent: {0}")]
    AmbiguousError(String),
    #[error("Unknown intent type")]
    UnknownIntent,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Intent {
    pub raw_text: String,
    pub intent_type: IntentType,
    pub target_app: Option<String>,
    pub target_action: Option<String>,
    pub entities: Vec<Entity>,
    pub confidence: f32,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum IntentType {
    SendMessage,
    Search,
    Navigate,
    OpenApp,
    ViewFile,
    Query,
    Unknown,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Entity {
    pub name: String,
    pub value: String,
    pub entity_type: EntityType,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum EntityType {
    Person,
    Location,
    App,
    Content,
    Time,
    Number,
}

pub struct IntentParser {
    chinese_patterns: Vec<(&'static str, IntentType)>,
}

impl IntentParser {
    pub fn new() -> Self {
        let patterns = vec![
            ("发消息", IntentType::SendMessage),
            ("发送消息", IntentType::SendMessage),
            ("发微信", IntentType::SendMessage),
            ("搜索", IntentType::Search),
            ("找一下", IntentType::Search),
            ("导航到", IntentType::Navigate),
            ("去", IntentType::Navigate),
            ("打开", IntentType::OpenApp),
            ("查看", IntentType::ViewFile),
            ("看看", IntentType::ViewFile),
            ("查询", IntentType::Query),
        ];
        Self {
            chinese_patterns: patterns,
        }
    }

    pub fn parse(&self, text: &str) -> Result<Intent, IntentError> {
        let mut intent_type = IntentType::Unknown;
        let mut confidence = 0.5;

        for (pattern, i_type) in &self.chinese_patterns {
            if text.contains(pattern) {
                intent_type = *i_type;
                confidence = 0.8;
                break;
            }
        }

        Ok(Intent {
            raw_text: text.to_string(),
            intent_type,
            target_app: None,
            target_action: None,
            entities: Vec::new(),
            confidence,
        })
    }
}

impl Default for IntentParser {
    fn default() -> Self {
        Self::new()
    }
}
