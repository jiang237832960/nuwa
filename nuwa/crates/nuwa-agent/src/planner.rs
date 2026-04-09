use serde::{Deserialize, Serialize};
use super::intent::Intent;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Task {
    pub id: String,
    pub intent: Intent,
    pub steps: Vec<Step>,
    pub created_at: u64,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Step {
    pub id: usize,
    pub action: Action,
    pub target: Option<Target>,
    pub fallback: Option<Box<Step>>,
    pub status: StepStatus,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum Action {
    Click,
    Input,
    Swipe,
    OpenApp,
    CloseApp,
    Wait,
    Query,
    Navigate,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Target {
    pub selector: Selector,
    pub text: Option<String>,
    pub bounds: Option<(i32, i32, i32, i32)>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Selector {
    pub by: SelectorType,
    pub value: String,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum SelectorType {
    Text,
    ContentDesc,
    ResourceId,
    ClassName,
    PackageName,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum StepStatus {
    Pending,
    Running,
    Success,
    Failed,
    Skipped,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct StepResult {
    pub step_id: usize,
    pub status: StepStatus,
    pub message: Option<String>,
    pub screenshot: Option<Vec<u8>>,
}

pub struct TaskPlanner {
    app_workflows: std::collections::HashMap<String, Vec<Step>>,
}

impl TaskPlanner {
    pub fn new() -> Self {
        let mut workflows = std::collections::HashMap::new();
        workflows.insert("wechat".to_string(), wechat_send_message_workflow());
        Self {
            app_workflows: workflows,
        }
    }

    pub fn plan(&self, intent: &Intent) -> Result<Task, crate::AgentError> {
        let steps = match intent.intent_type {
            super::intent::IntentType::SendMessage => {
                if let Some(app) = &intent.target_app {
                    self.app_workflows.get(app).cloned().unwrap_or_default()
                } else {
                    vec![]
                }
            }
            _ => vec![],
        };

        Ok(Task {
            id: format!("task_{}", std::time::SystemTime::now()
                .duration_since(std::time::UNIX_EPOCH)
                .unwrap()
                .as_millis()),
            intent: intent.clone(),
            steps,
            created_at: std::time::SystemTime::now()
                .duration_since(std::time::UNIX_EPOCH)
                .unwrap()
                .as_secs(),
        })
    }
}

impl Default for TaskPlanner {
    fn default() -> Self {
        Self::new()
    }
}

fn wechat_send_message_workflow() -> Vec<Step> {
    vec![
        Step {
            id: 1,
            action: Action::OpenApp,
            target: Some(Target {
                selector: Selector {
                    by: SelectorType::PackageName,
                    value: "com.tencent.mm".to_string(),
                },
                text: None,
                bounds: None,
            }),
            fallback: None,
            status: StepStatus::Pending,
        },
        Step {
            id: 2,
            action: Action::Wait,
            target: None,
            fallback: None,
            status: StepStatus::Pending,
        },
        Step {
            id: 3,
            action: Action::Click,
            target: Some(Target {
                selector: Selector {
                    by: SelectorType::Text,
                    value: "搜索".to_string(),
                },
                text: None,
                bounds: None,
            }),
            fallback: None,
            status: StepStatus::Pending,
        },
    ]
}
