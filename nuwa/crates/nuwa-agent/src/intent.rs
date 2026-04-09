use serde::{Deserialize, Serialize};
use thiserror::Error;
use std::collections::HashMap;

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
    SearchContact,
    Navigate,
    OpenApp,
    ViewFile,
    Query,
    SaveImage,
    QueryBill,
    Unknown,
}

impl IntentType {
    pub fn as_str(&self) -> &'static str {
        match self {
            IntentType::SendMessage => "SendMessage",
            IntentType::SearchContact => "SearchContact",
            IntentType::Navigate => "Navigate",
            IntentType::OpenApp => "OpenApp",
            IntentType::ViewFile => "ViewFile",
            IntentType::Query => "Query",
            IntentType::SaveImage => "SaveImage",
            IntentType::QueryBill => "QueryBill",
            IntentType::Unknown => "Unknown",
        }
    }
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
    intent_patterns: Vec<(&'static str, IntentType)>,
    app_patterns: HashMap<&'static str, &'static str>,
    entity_patterns: Vec<(&'static str, EntityType)>,
}

impl IntentParser {
    pub fn new() -> Self {
        let intent_patterns = vec![
            ("发消息", IntentType::SendMessage),
            ("发送消息", IntentType::SendMessage),
            ("发微信", IntentType::SendMessage),
            ("微信发消息", IntentType::SendMessage),
            ("搜索联系人", IntentType::SearchContact),
            ("找人", IntentType::SearchContact),
            ("查找联系人", IntentType::SearchContact),
            ("导航到", IntentType::Navigate),
            ("导航去", IntentType::Navigate),
            ("去", IntentType::Navigate),
            ("到", IntentType::Navigate),
            ("打开微信", IntentType::OpenApp),
            ("启动微信", IntentType::OpenApp),
            ("打开地图", IntentType::OpenApp),
            ("打开高德", IntentType::OpenApp),
            ("打开WPS", IntentType::OpenApp),
            ("打开文档", IntentType::ViewFile),
            ("打开文件", IntentType::ViewFile),
            ("查看文件", IntentType::ViewFile),
            ("保存图片", IntentType::SaveImage),
            ("下载图片", IntentType::SaveImage),
            ("保存图片到", IntentType::SaveImage),
            ("查询账单", IntentType::QueryBill),
            ("查看账单", IntentType::QueryBill),
            ("账单查询", IntentType::QueryBill),
            ("搜索", IntentType::SearchContact),
            ("找一下", IntentType::SearchContact),
            ("找找", IntentType::SearchContact),
            ("查询", IntentType::Query),
        ];
        
        let mut app_patterns = HashMap::new();
        app_patterns.insert("微信", "com.tencent.mm");
        app_patterns.insert("wechat", "com.tencent.mm");
        app_patterns.insert("高德", "com.autonavi.minimap");
        app_patterns.insert("地图", "com.autonavi.minimap");
        app_patterns.insert("支付宝", "com.eg.android.AlipayGphone");
        app_patterns.insert("alipay", "com.eg.android.AlipayGphone");
        app_patterns.insert("wps", "cn.wps.moffice_eng");
        app_patterns.insert("淘宝", "com.taobao.taobao");
        app_patterns.insert("京东", "com.jingdong.app.mall");
        app_patterns.insert("美团", "com.sankuai.meituan");
        app_patterns.insert("12306", "com.MobileTicket");
        app_patterns.insert("钉钉", "com.alibaba.android.rimet");
        app_patterns.insert("企业微信", "com.tencent.wework");
        
        let entity_patterns = vec![
            ("给", EntityType::Person),
            ("向", EntityType::Person),
            ("发给", EntityType::Person),
        ];
        
        Self {
            intent_patterns,
            app_patterns,
            entity_patterns,
        }
    }

    pub fn parse(&self, text: &str) -> Result<Intent, IntentError> {
        let intent_type = self.detect_intent_type(text);
        let target_app = self.detect_target_app(text);
        let entities = self.extract_entities(text, &intent_type);
        let confidence = self.calculate_confidence(&intent_type, &entities, text);
        
        let target_action = self.infer_action(&intent_type, &entities);
        
        Ok(Intent {
            raw_text: text.to_string(),
            intent_type,
            target_app,
            target_action,
            entities,
            confidence,
        })
    }
    
    fn detect_intent_type(&self, text: &str) -> IntentType {
        let mut best_match = IntentType::Unknown;
        let mut best_len = 0;
        
        for (pattern, intent_type) in &self.intent_patterns {
            if text.contains(pattern) && pattern.len() > best_len {
                best_match = *intent_type;
                best_len = pattern.len();
            }
        }
        
        best_match
    }
    
    fn detect_target_app(&self, text: &str) -> Option<String> {
        for (pattern, app_package) in &self.app_patterns {
            if text.contains(pattern) {
                return Some(app_package.to_string());
            }
        }
        None
    }
    
    fn extract_entities(&self, text: &str, intent_type: &IntentType) -> Vec<Entity> {
        let mut entities = Vec::new();
        
        match intent_type {
            IntentType::SendMessage => {
                if let Some(contact) = self.extract_contact_name(text) {
                    entities.push(Entity {
                        name: "联系人".to_string(),
                        value: contact,
                        entity_type: EntityType::Person,
                    });
                }
                
                if let Some(content) = self.extract_message_content(text) {
                    entities.push(Entity {
                        name: "消息内容".to_string(),
                        value: content,
                        entity_type: EntityType::Content,
                    });
                }
            }
            IntentType::Navigate => {
                if let Some(location) = self.extract_location(text) {
                    entities.push(Entity {
                        name: "目的地".to_string(),
                        value: location,
                        entity_type: EntityType::Location,
                    });
                }
            }
            IntentType::SaveImage => {
                if let Some(path) = self.extract_save_path(text) {
                    entities.push(Entity {
                        name: "保存路径".to_string(),
                        value: path,
                        entity_type: EntityType::Location,
                    });
                }
            }
            _ => {}
        }
        
        entities
    }
    
    fn extract_contact_name(&self, text: &str) -> Option<String> {
        let patterns = [
            r"给(.+?)发消息",
            r"向(.+?)发消息",
            r"发给(.+?)(?:的|发|消息)",
            r"给(.+?)发",
            r"发给(.+)",
            r"和(.+?)说",
            r"跟(.+?)说",
        ];
        
        for pattern in &patterns {
            if let Ok(re) = regex::Regex::new(pattern) {
                if let Some(caps) = re.captures(text) {
                    if let Some(name) = caps.get(1) {
                        let name_str = name.as_str().trim();
                        if !name_str.is_empty() && name_str.len() <= 20 {
                            return Some(name_str.to_string());
                        }
                    }
                }
            }
        }
        
        None
    }
    
    fn extract_message_content(&self, text: &str) -> Option<String> {
        let patterns = [
            r"说(.+?)$",
            r"告诉她?(.+?)$",
            r"内容是(.+?)$",
            r"消息是(.+?)$",
        ];
        
        for pattern in &patterns {
            if let Ok(re) = regex::Regex::new(pattern) {
                if let Some(caps) = re.captures(text) {
                    if let Some(content) = caps.get(1) {
                        let content_str = content.as_str().trim();
                        if !content_str.is_empty() && content_str.len() <= 500 {
                            return Some(content_str.to_string());
                        }
                    }
                }
            }
        }
        
        None
    }
    
    fn extract_location(&self, text: &str) -> Option<String> {
        let patterns = [
            r"导航到(.+?)(?:的|那里|一下|$)",
            r"去(.+?)(?:那里|一下|$)",
            r"到(.+?)(?:那里|一下|$)",
            r"目的地是(.+?)$",
        ];
        
        for pattern in &patterns {
            if let Ok(re) = regex::Regex::new(pattern) {
                if let Some(caps) = re.captures(text) {
                    if let Some(loc) = caps.get(1) {
                        let loc_str = loc.as_str().trim();
                        if !loc_str.is_empty() && loc_str.len() <= 100 {
                            return Some(loc_str.to_string());
                        }
                    }
                }
            }
        }
        
        None
    }
    
    fn extract_save_path(&self, text: &str) -> Option<String> {
        let patterns = [
            r"保存到(.+?)$",
            r"存到(.+?)$",
            r"下载到(.+?)$",
        ];
        
        for pattern in &patterns {
            if let Ok(re) = regex::Regex::new(pattern) {
                if let Some(caps) = re.captures(text) {
                    if let Some(path) = caps.get(1) {
                        return Some(path.as_str().trim().to_string());
                    }
                }
            }
        }
        
        Some("Pictures".to_string())
    }
    
    fn infer_action(&self, intent_type: &IntentType, entities: &[Entity]) -> Option<String> {
        match intent_type {
            IntentType::SendMessage => Some("send_message".to_string()),
            IntentType::Navigate => Some("navigate".to_string()),
            IntentType::ViewFile => Some("open_file".to_string()),
            IntentType::QueryBill => Some("query_bill".to_string()),
            IntentType::SaveImage => Some("save_image".to_string()),
            _ => None,
        }
    }
    
    fn calculate_confidence(&self, intent_type: &IntentType, entities: &[Entity], text: &str) -> f32 {
        if *intent_type == IntentType::Unknown {
            return 0.3;
        }
        
        let mut confidence: f32 = 0.6;
        
        if !entities.is_empty() {
            confidence += 0.15;
        }
        
        let has_app_mention = self.detect_target_app(text).is_some();
        if has_app_mention {
            confidence += 0.15;
        }
        
        confidence.min(1.0)
    }
    
    pub fn to_json(&self, intent: &Intent) -> String {
        serde_json::to_string(intent).unwrap_or_default()
    }
    
    pub fn from_json(&self, json: &str) -> Result<Intent, IntentError> {
        serde_json::from_str(json).map_err(|e| IntentError::ParseError(e.to_string()))
    }
}

impl Default for IntentParser {
    fn default() -> Self {
        Self::new()
    }
}

pub fn create_intent_parser() -> IntentParser {
    IntentParser::new()
}
