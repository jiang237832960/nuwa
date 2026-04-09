use serde::{Deserialize, Serialize};
use super::intent::Intent;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Task {
    pub id: String,
    pub intent: Intent,
    pub steps: Vec<Step>,
    pub created_at: u64,
    pub status: TaskStatus,
    pub current_step: usize,
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
pub struct Step {
    pub id: usize,
    pub action: Action,
    pub target: Option<Target>,
    pub fallback: Option<Box<Step>>,
    pub status: StepStatus,
    pub message: Option<String>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum Action {
    OpenApp,
    Click,
    Input,
    Swipe,
    CloseApp,
    Wait,
    Query,
    Navigate,
    Scroll,
    LongClick,
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
    workflows: std::collections::HashMap<String, Workflow>,
}

struct Workflow {
    name: String,
    steps_fn: fn(&Intent) -> Vec<Step>,
}

impl TaskPlanner {
    pub fn new() -> Self {
        let mut workflows = std::collections::HashMap::new();
        
        workflows.insert("wechat_send_message".to_string(), Workflow {
            name: "微信发消息".to_string(),
            steps_fn: wechat_send_message_steps,
        });
        
        workflows.insert("wechat_save_image".to_string(), Workflow {
            name: "微信保存图片".to_string(),
            steps_fn: wechat_save_image_steps,
        });
        
        workflows.insert("amap_navigate".to_string(), Workflow {
            name: "高德地图导航".to_string(),
            steps_fn: amap_navigate_steps,
        });
        
        workflows.insert("wps_open_file".to_string(), Workflow {
            name: "WPS打开文件".to_string(),
            steps_fn: wps_open_file_steps,
        });
        
        workflows.insert("alipay_query_bill".to_string(), Workflow {
            name: "支付宝查账单".to_string(),
            steps_fn: alipay_query_bill_steps,
        });
        
        Self { workflows }
    }

    pub fn plan(&self, intent: &Intent) -> Result<Task, crate::AgentError> {
        let steps = self.generate_steps(intent);
        
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
            status: TaskStatus::Idle,
            current_step: 0,
        })
    }
    
    fn generate_steps(&self, intent: &Intent) -> Vec<Step> {
        let app = intent.target_app.as_deref().unwrap_or("");
        
        match intent.intent_type {
            super::intent::IntentType::SendMessage if app == "com.tencent.mm" => {
                (wechat_send_message_steps)(intent)
            }
            super::intent::IntentType::SaveImage if app == "com.tencent.mm" => {
                (wechat_save_image_steps)(intent)
            }
            super::intent::IntentType::Navigate if app == "com.autonavi.minimap" => {
                (amap_navigate_steps)(intent)
            }
            super::intent::IntentType::ViewFile if app == "cn.wps.moffice_eng" => {
                (wps_open_file_steps)(intent)
            }
            super::intent::IntentType::QueryBill if app == "com.eg.android.AlipayGphone" => {
                (alipay_query_bill_steps)(intent)
            }
            super::intent::IntentType::SendMessage => {
                (wechat_send_message_steps)(intent)
            }
            super::intent::IntentType::Navigate => {
                (amap_navigate_steps)(intent)
            }
            super::intent::IntentType::ViewFile => {
                (wps_open_file_steps)(intent)
            }
            _ => {
                vec![]
            }
        }
    }
    
    pub fn to_json(&self, task: &Task) -> String {
        serde_json::to_string(task).unwrap_or_default()
    }
    
    pub fn from_json(&self, json: &str) -> Result<Task, serde_json::Error> {
        serde_json::from_str(json)
    }
}

impl Default for TaskPlanner {
    fn default() -> Self {
        Self::new()
    }
}

fn wechat_send_message_steps(intent: &Intent) -> Vec<Step> {
    let contact = intent.entities.iter()
        .find(|e| e.entity_type == super::intent::EntityType::Person)
        .map(|e| e.value.clone())
        .unwrap_or_default();
    
    let message = intent.entities.iter()
        .find(|e| e.entity_type == super::intent::EntityType::Content)
        .map(|e| e.value.clone())
        .unwrap_or_default();
    
    let mut steps = vec![
        step(1, Action::OpenApp, Some(target_pkg("com.tencent.mm")), Some("打开微信")),
        step(2, Action::Wait, None, Some("等待界面加载")),
        step(3, Action::Click, Some(target_text("通讯录")), Some("点击通讯录")),
        step(4, Action::Wait, None, Some("等待通讯录加载")),
        step(5, Action::Click, Some(target_text("新的朋友")), Some("点击新的朋友")),
    ];
    
    if !contact.is_empty() {
        steps.push(step(6, Action::Click, Some(target_text("搜索")), Some("点击搜索")));
        steps.push(step(7, Action::Wait, None, Some("等待搜索框")));
        steps.push(step(8, Action::Click, Some(target_res_id("com.tencent.mm:id/con")), Some("点击搜索输入框")));
        steps.push(step(9, Action::Input, Some(target_class("android.widget.EditText")), Some(&format!("输入联系人: {}", contact))));
    }
    
    if !message.is_empty() {
        steps.push(step(10, Action::Wait, None, Some("等待聊天界面")));
        steps.push(step(11, Action::Click, Some(target_res_id("com.tencent.mm:id/aq0")), Some("点击消息输入框")));
        steps.push(step(12, Action::Input, Some(target_class("android.widget.EditText")), Some(&format!("输入消息: {}", message))));
        steps.push(step(13, Action::Click, Some(target_text("发送")), Some("点击发送")));
    }
    
    steps
}

fn wechat_save_image_steps(intent: &Intent) -> Vec<Step> {
    vec![
        step(1, Action::OpenApp, Some(target_pkg("com.tencent.mm")), Some("打开微信")),
        step(2, Action::Wait, None, Some("等待界面加载")),
        step(3, Action::LongClick, Some(target_text("图片")), Some("长按图片")),
        step(4, Action::Wait, None, Some("等待弹出菜单")),
        step(5, Action::Click, Some(target_text("保存图片")), Some("点击保存图片")),
        step(6, Action::Click, Some(target_text("保存到相册")), Some("点击保存到相册")),
    ]
}

fn amap_navigate_steps(intent: &Intent) -> Vec<Step> {
    let destination = intent.entities.iter()
        .find(|e| e.entity_type == super::intent::EntityType::Location)
        .map(|e| e.value.clone())
        .unwrap_or_default();
    
    let mut steps = vec![
        step(1, Action::OpenApp, Some(target_pkg("com.autonavi.minimap")), Some("打开高德地图")),
        step(2, Action::Wait, None, Some("等待地图加载")),
        step(3, Action::Click, Some(target_res_id("com.autonavi.minimap:id/bbv")), Some("点击搜索框")),
    ];
    
    if !destination.is_empty() {
        steps.push(step(4, Action::Wait, None, Some("等待搜索界面")));
        steps.push(step(5, Action::Click, Some(target_class("android.widget.EditText")), Some("点击输入框")));
        steps.push(step(6, Action::Input, Some(target_class("android.widget.EditText")), Some(&format!("输入目的地: {}", destination))));
        steps.push(step(7, Action::Wait, None, Some("等待搜索结果")));
        steps.push(step(8, Action::Click, Some(target_text("导航")), Some("点击导航")));
    }
    
    steps
}

fn wps_open_file_steps(intent: &Intent) -> Vec<Step> {
    vec![
        step(1, Action::OpenApp, Some(target_pkg("cn.wps.moffice_eng")), Some("打开WPS")),
        step(2, Action::Wait, None, Some("等待WPS加载")),
        step(3, Action::Click, Some(target_text("打开文档")), Some("点击打开文档")),
        step(4, Action::Wait, None, Some("等待文件选择器")),
        step(5, Action::Click, Some(target_text("最近")), Some("点击最近")),
    ]
}

fn alipay_query_bill_steps(intent: &Intent) -> Vec<Step> {
    vec![
        step(1, Action::OpenApp, Some(target_pkg("com.eg.android.AlipayGphone")), Some("打开支付宝")),
        step(2, Action::Wait, None, Some("等待支付宝加载")),
        step(3, Action::Click, Some(target_text("我的")), Some("点击我的")),
        step(4, Action::Wait, None, Some("等待页面加载")),
        step(5, Action::Click, Some(target_text("账单")), Some("点击账单")),
        step(6, Action::Wait, None, Some("等待账单加载")),
    ]
}

fn step(id: usize, action: Action, target: Option<Target>, message: Option<&str>) -> Step {
    Step {
        id,
        action,
        target,
        fallback: None,
        status: StepStatus::Pending,
        message: message.map(|s| s.to_string()),
    }
}

fn target_pkg(pkg: &str) -> Target {
    Target {
        selector: Selector {
            by: SelectorType::PackageName,
            value: pkg.to_string(),
        },
        text: None,
        bounds: None,
    }
}

fn target_text(text: &str) -> Target {
    Target {
        selector: Selector {
            by: SelectorType::Text,
            value: text.to_string(),
        },
        text: None,
        bounds: None,
    }
}

fn target_res_id(id: &str) -> Target {
    Target {
        selector: Selector {
            by: SelectorType::ResourceId,
            value: id.to_string(),
        },
        text: None,
        bounds: None,
    }
}

fn target_class(class: &str) -> Target {
    Target {
        selector: Selector {
            by: SelectorType::ClassName,
            value: class.to_string(),
        },
        text: None,
        bounds: None,
    }
}
