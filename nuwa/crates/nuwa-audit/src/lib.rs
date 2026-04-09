use serde::{Deserialize, Serialize};

pub struct Audit {
    records: Vec<AuditRecord>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AuditRecord {
    pub timestamp: u64,
    pub event_type: EventType,
    pub details: String,
    pub hash: Option<String>,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum EventType {
    TaskStart,
    TaskComplete,
    TaskFail,
    ActionExecute,
    ResourceUpdate,
    GrowthRequest,
    Negotiation,
}

impl Audit {
    pub fn new() -> Self {
        Self {
            records: Vec::new(),
        }
    }

    pub fn log(&mut self, event: EventType, details: &str) {
        self.records.push(AuditRecord {
            timestamp: std::time::SystemTime::now()
                .duration_since(std::time::UNIX_EPOCH)
                .unwrap()
                .as_secs(),
            event_type: event,
            details: details.to_string(),
            hash: None,
        });
    }

    pub fn export_jsonl(&self) -> String {
        self.records
            .iter()
            .map(|r| serde_json::to_string(r).unwrap_or_default())
            .collect::<Vec<_>>()
            .join("\n")
    }
}

impl Default for Audit {
    fn default() -> Self {
        Self::new()
    }
}
