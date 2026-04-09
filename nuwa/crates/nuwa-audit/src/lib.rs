use serde::{Deserialize, Serialize};
use parking_lot::RwLock;
use std::sync::Arc;
use std::collections::VecDeque;

const MAX_RECORDS: usize = 10000;

pub struct Audit {
    records: Arc<RwLock<VecDeque<AuditRecord>>>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AuditRecord {
    pub timestamp: u64,
    pub event_type: EventType,
    pub details: String,
    pub task_id: Option<String>,
    pub step_id: Option<usize>,
    pub hash: Option<String>,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum EventType {
    TaskStart,
    TaskComplete,
    TaskFail,
    TaskCancel,
    ActionExecute,
    ActionResult,
    ActionFail,
    ResourceUpdate,
    GrowthRequest,
    Negotiation,
    IntentParse,
    PlanningComplete,
    Error,
}

impl Audit {
    pub fn new() -> Self {
        Self {
            records: Arc::new(RwLock::new(VecDeque::with_capacity(MAX_RECORDS))),
        }
    }

    pub fn log(&self, event: EventType, details: &str) {
        self.log_with_context(event, details, None, None);
    }

    pub fn log_task(&self, event: EventType, task_id: &str, details: &str) {
        self.log_with_context(event, details, Some(task_id.to_string()), None);
    }

    pub fn log_step(&self, event: EventType, task_id: &str, step_id: usize, details: &str) {
        self.log_with_context(event, details, Some(task_id.to_string()), Some(step_id));
    }

    fn log_with_context(&self, event: EventType, details: &str, task_id: Option<String>, step_id: Option<usize>) {
        let mut records = self.records.write();
        
        if records.len() >= MAX_RECORDS {
            records.pop_front();
        }
        
        let timestamp = std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .unwrap()
            .as_secs();
        
        records.push_back(AuditRecord {
            timestamp,
            event_type: event,
            details: details.to_string(),
            task_id,
            step_id,
            hash: None,
        });
    }

    pub fn get_records(&self, limit: Option<usize>) -> Vec<AuditRecord> {
        let records = self.records.read();
        let records_vec: Vec<AuditRecord> = records.iter().cloned().collect();
        
        match limit {
            Some(n) => records_vec.into_iter().rev().take(n).collect::<Vec<_>>().into_iter().rev().collect(),
            None => records_vec,
        }
    }

    pub fn get_task_records(&self, task_id: &str) -> Vec<AuditRecord> {
        let records = self.records.read();
        records
            .iter()
            .filter(|r| r.task_id.as_deref() == Some(task_id))
            .cloned()
            .collect()
    }

    pub fn get_recent_errors(&self, limit: usize) -> Vec<AuditRecord> {
        let records = self.records.read();
        records
            .iter()
            .filter(|r| matches!(r.event_type, EventType::Error | EventType::ActionFail | EventType::TaskFail))
            .rev()
            .take(limit)
            .cloned()
            .collect()
    }

    pub fn export_jsonl(&self) -> String {
        let records = self.records.read();
        records
            .iter()
            .map(|r| serde_json::to_string(r).unwrap_or_default())
            .collect::<Vec<_>>()
            .join("\n")
    }

    pub fn export_task_jsonl(&self, task_id: &str) -> String {
        self.get_task_records(task_id)
            .iter()
            .map(|r| serde_json::to_string(r).unwrap_or_default())
            .collect::<Vec<_>>()
            .join("\n")
    }

    pub fn clear(&self) {
        self.records.write().clear();
    }

    pub fn record_count(&self) -> usize {
        self.records.read().len()
    }

    pub fn log_intent_parse(&self, text: &str, intent_type: &str, confidence: f32) {
        self.log(EventType::IntentParse, &format!("text={}, intent={}, confidence={}", text, intent_type, confidence));
    }

    pub fn log_planning(&self, task_id: &str, step_count: usize) {
        self.log_task(EventType::PlanningComplete, task_id, &format!("Generated {} steps", step_count));
    }

    pub fn log_action_execute(&self, task_id: &str, step_id: usize, action: &str, target: Option<&str>) {
        let details = match target {
            Some(t) => format!("action={}, target={}", action, t),
            None => format!("action={}", action),
        };
        self.log_step(EventType::ActionExecute, task_id, step_id, &details);
    }

    pub fn log_action_result(&self, task_id: &str, step_id: usize, success: bool, message: Option<&str>) {
        let event = if success { EventType::ActionResult } else { EventType::ActionFail };
        let details = match (success, message) {
            (true, Some(m)) => format!("success, {}", m),
            (true, None) => "success".to_string(),
            (false, Some(m)) => format!("failed, {}", m),
            (false, None) => "failed".to_string(),
        };
        self.log_step(event, task_id, step_id, &details);
    }

    pub fn log_task_start(&self, task_id: &str, intent_type: &str) {
        self.log_task(EventType::TaskStart, task_id, &format!("intent={}", intent_type));
    }

    pub fn log_task_complete(&self, task_id: &str, steps_completed: usize, steps_failed: usize) {
        let details = format!("completed={}, failed={}", steps_completed, steps_failed);
        self.log_task(EventType::TaskComplete, task_id, &details);
    }

    pub fn log_task_fail(&self, task_id: &str, reason: &str) {
        self.log_task(EventType::TaskFail, task_id, reason);
    }

    pub fn log_error(&self, error: &str) {
        self.log(EventType::Error, error);
    }
}

impl Default for Audit {
    fn default() -> Self {
        Self::new()
    }
}

pub fn create_audit() -> Audit {
    Audit::new()
}
