use serde::{Deserialize, Serialize};

pub struct Memory {
    layers: MemoryLayers,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct MemoryLayers {
    pub working: WorkingMemory,
    pub short_term: ShortTermMemory,
    pub long_term: LongTermMemory,
    pub contextual: ContextualMemory,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct WorkingMemory {
    pub current_task: Option<String>,
    pub current_context: Vec<String>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ShortTermMemory {
    pub recent_events: Vec<MemoryEvent>,
    pub expiry: u64,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct MemoryEvent {
    pub timestamp: u64,
    pub content: String,
    pub importance: f32,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct LongTermMemory {
    pub user_preferences: Vec<Preference>,
    pub habits: Vec<Habit>,
    pub knowledge: Vec<KnowledgeEntry>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Preference {
    pub key: String,
    pub value: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Habit {
    pub description: String,
    pub frequency: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct KnowledgeEntry {
    pub topic: String,
    pub content: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ContextualMemory {
    pub user_name: Option<String>,
    pub call_name: Option<String>,
    pub relationship_mode: Option<String>,
}

impl Memory {
    pub fn new() -> Self {
        Self {
            layers: MemoryLayers {
                working: WorkingMemory {
                    current_task: None,
                    current_context: Vec::new(),
                },
                short_term: ShortTermMemory {
                    recent_events: Vec::new(),
                    expiry: 3600,
                },
                long_term: LongTermMemory {
                    user_preferences: Vec::new(),
                    habits: Vec::new(),
                    knowledge: Vec::new(),
                },
                contextual: ContextualMemory {
                    user_name: None,
                    call_name: None,
                    relationship_mode: None,
                },
            },
        }
    }

    pub fn set_working_task(&mut self, task: &str) {
        self.layers.working.current_task = Some(task.to_string());
    }

    pub fn add_short_term_event(&mut self, content: &str) {
        self.layers.short_term.recent_events.push(MemoryEvent {
            timestamp: std::time::SystemTime::now()
                .duration_since(std::time::UNIX_EPOCH)
                .unwrap()
                .as_secs(),
            content: content.to_string(),
            importance: 0.5,
        });
    }
}

impl Default for Memory {
    fn default() -> Self {
        Self::new()
    }
}
