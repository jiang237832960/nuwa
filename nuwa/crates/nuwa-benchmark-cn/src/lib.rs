use serde::{Deserialize, Serialize};
use crate::nuwa_agent::{Task, StepResult};
use crate::world::WorldState;

pub struct BenchmarkCN {
    tasks: Vec<BenchmarkTask>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct BenchmarkTask {
    pub id: String,
    pub name: String,
    pub app: String,
    pub description: String,
    pub steps: Vec<String>,
    pub expected_success_rate: f32,
}

impl BenchmarkCN {
    pub fn new() -> Self {
        Self {
            tasks: vec![
                BenchmarkTask {
                    id: "wechat_send_text".to_string(),
                    name: "微信发送文本消息".to_string(),
                    app: "wechat".to_string(),
                    description: "给指定联系人发送文本消息".to_string(),
                    steps: vec![
                        "打开微信".to_string(),
                        "点击搜索".to_string(),
                        "输入联系人名称".to_string(),
                        "选择联系人".to_string(),
                        "输入消息".to_string(),
                        "发送".to_string(),
                    ],
                    expected_success_rate: 0.85,
                },
                BenchmarkTask {
                    id: "amap_navigate".to_string(),
                    name: "高德地图发起导航".to_string(),
                    app: "amap".to_string(),
                    description: "从当前位置发起导航到目标地点".to_string(),
                    steps: vec![
                        "打开高德地图".to_string(),
                        "点击搜索框".to_string(),
                        "输入目的地".to_string(),
                        "选择第一个结果".to_string(),
                        "点击导航".to_string(),
                    ],
                    expected_success_rate: 0.80,
                },
                BenchmarkTask {
                    id: "wps_open_doc".to_string(),
                    name: "WPS打开文档".to_string(),
                    app: "wps".to_string(),
                    description: "打开指定路径的文档".to_string(),
                    steps: vec![
                        "打开WPS".to_string(),
                        "点击最近文档".to_string(),
                        "选择文档".to_string(),
                    ],
                    expected_success_rate: 0.90,
                },
            ],
        }
    }

    pub fn get_tasks(&self) -> &[BenchmarkTask] {
        &self.tasks
    }

    pub fn evaluate(&self, task_id: &str, result: &StepResult, state: &WorldState) -> BenchmarkResult {
        let task = self.tasks.iter().find(|t| t.id == task_id);
        match task {
            Some(t) => BenchmarkResult {
                task_id: task_id.to_string(),
                success: result.status == crate::nuwa_agent::StepStatus::Success,
                efficiency_score: 0.8,
                stability_score: 0.8,
                meets_expectation: result.status == crate::nuwa_agent::StepStatus::Success,
            },
            None => BenchmarkResult {
                task_id: task_id.to_string(),
                success: false,
                efficiency_score: 0.0,
                stability_score: 0.0,
                meets_expectation: false,
            },
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct BenchmarkResult {
    pub task_id: String,
    pub success: bool,
    pub efficiency_score: f32,
    pub stability_score: f32,
    pub meets_expectation: bool,
}

impl Default for BenchmarkCN {
    fn default() -> Self {
        Self::new()
    }
}
