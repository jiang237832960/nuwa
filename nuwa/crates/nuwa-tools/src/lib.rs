use serde::{Deserialize, Serialize};
use std::path::Path;
use parking_lot::RwLock;
use std::sync::Arc;

pub struct Tools {
    registry: Arc<RwLock<ToolRegistry>>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ToolDef {
    pub id: String,
    pub name: String,
    pub description: String,
    pub params: Vec<ParamDef>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ParamDef {
    pub name: String,
    pub param_type: ParamType,
    pub required: bool,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum ParamType {
    String,
    Number,
    Boolean,
    Object,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ToolResult {
    pub tool_id: String,
    pub success: bool,
    pub output: Option<String>,
    pub error: Option<String>,
}

pub struct ToolRegistry {
    tools: Vec<ToolDef>,
}

impl ToolRegistry {
    pub fn new() -> Self {
        let mut tools = Vec::new();
        tools.push(ToolDef {
            id: "click".to_string(),
            name: "点击".to_string(),
            description: "点击屏幕指定位置".to_string(),
            params: vec![
                ParamDef { name: "x".to_string(), param_type: ParamType::Number, required: true },
                ParamDef { name: "y".to_string(), param_type: ParamType::Number, required: true },
            ],
        });
        tools.push(ToolDef {
            id: "input_text".to_string(),
            name: "输入文本".to_string(),
            description: "在焦点处输入文本".to_string(),
            params: vec![
                ParamDef { name: "text".to_string(), param_type: ParamType::String, required: true },
            ],
        });
        tools.push(ToolDef {
            id: "swipe".to_string(),
            name: "滑动".to_string(),
            description: "从一点滑动到另一点".to_string(),
            params: vec![
                ParamDef { name: "x1".to_string(), param_type: ParamType::Number, required: true },
                ParamDef { name: "y1".to_string(), param_type: ParamType::Number, required: true },
                ParamDef { name: "x2".to_string(), param_type: ParamType::Number, required: true },
                ParamDef { name: "y2".to_string(), param_type: ParamType::Number, required: true },
            ],
        });
        Self { tools }
    }

    pub fn get_tool(&self, id: &str) -> Option<&ToolDef> {
        self.tools.iter().find(|t| t.id == id)
    }

    pub fn list_tools(&self) -> &[ToolDef] {
        &self.tools
    }
}

impl Tools {
    pub fn new() -> Self {
        Self {
            registry: Arc::new(RwLock::new(ToolRegistry::new())),
        }
    }

    pub fn execute(&self, tool_id: &str, params: &serde_json::Value) -> ToolResult {
        let registry = self.registry.read();
        match registry.get_tool(tool_id) {
            Some(_) => ToolResult {
                tool_id: tool_id.to_string(),
                success: true,
                output: Some("{}".to_string()),
                error: None,
            },
            None => ToolResult {
                tool_id: tool_id.to_string(),
                success: false,
                output: None,
                error: Some(format!("Tool {} not found", tool_id)),
            },
        }
    }
}

impl Default for Tools {
    fn default() -> Self {
        Self::new()
    }
}
