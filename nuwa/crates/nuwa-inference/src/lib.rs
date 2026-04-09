mod gguf;
mod llama;

pub use gguf::{GGUFMeta, GGUFError};
pub use llama::{LlamaEngine, LlamaError};

use thiserror::Error;
use std::path::Path;

#[derive(Error, Debug)]
pub enum InferenceError {
    #[error("Model loading failed: {0}")]
    LoadError(String),
    #[error("Inference failed: {0}")]
    InferenceError(String),
    #[error("Tokenization failed: {0}")]
    TokenizeError(String),
}

pub trait InferenceEngine: Send + Sync {
    fn load_model(&mut self, path: &Path) -> Result<ModelHandle, InferenceError>;
    fn generate(&self, handle: &ModelHandle, prompt: &str) -> Result<String, InferenceError>;
    fn get_token_count(&self, text: &str) -> usize;
}

#[derive(Debug, Clone)]
pub struct ModelHandle {
    pub id: u64,
    pub path: String,
    pub meta: GGUFMeta,
}

pub struct ModelRegistry {
    models: std::collections::HashMap<u64, ModelHandle>,
    next_id: u64,
}

impl ModelRegistry {
    pub fn new() -> Self {
        Self {
            models: std::collections::HashMap::new(),
            next_id: 1,
        }
    }

    pub fn register(&mut self, handle: ModelHandle) -> u64 {
        let id = self.next_id;
        self.next_id += 1;
        self.models.insert(id, handle);
        id
    }

    pub fn get(&self, id: u64) -> Option<&ModelHandle> {
        self.models.get(&id)
    }
}
