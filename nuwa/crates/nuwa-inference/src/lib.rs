mod gguf;
mod llama;

pub use gguf::{GGUFMeta, GGUFError};
pub use llama::{LlamaEngine, LlamaError};

use thiserror::Error;
use std::path::Path;
use std::sync::Arc;
use parking_lot::RwLock;

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

pub struct NuwaInferenceEngine {
    engine: LlamaEngine,
    models: Arc<RwLock<ModelRegistry>>,
    current_model: Arc<RwLock<Option<ModelHandle>>>,
}

impl NuwaInferenceEngine {
    pub fn new() -> Self {
        Self {
            engine: LlamaEngine::new(),
            models: Arc::new(RwLock::new(ModelRegistry::new())),
            current_model: Arc::new(RwLock::new(None)),
        }
    }

    pub fn load_model(&mut self, path: &Path) -> Result<ModelHandle, InferenceError> {
        let handle = self.engine.load_model(path)?;
        let mut registry = self.models.write();
        let id = registry.register(handle.clone());
        let mut current = self.current_model.write();
        *current = Some(handle.clone());
        Ok(ModelHandle { id, ..handle })
    }

    pub fn generate(&self, prompt: &str) -> Result<String, InferenceError> {
        let current = self.current_model.read();
        if let Some(ref handle) = *current {
            self.engine.generate(handle, prompt)
        } else {
            Err(InferenceError::InferenceError("No model loaded".to_string()))
        }
    }

    pub fn get_token_count(&self, text: &str) -> usize {
        self.engine.get_token_count(text)
    }
}

impl Default for NuwaInferenceEngine {
    fn default() -> Self {
        Self::new()
    }
}

impl InferenceEngine for NuwaInferenceEngine {
    fn load_model(&mut self, path: &Path) -> Result<ModelHandle, InferenceError> {
        self.load_model(path)
    }

    fn generate(&self, handle: &ModelHandle, prompt: &str) -> Result<String, InferenceError> {
        self.engine.generate(handle, prompt)
    }

    fn get_token_count(&self, text: &str) -> usize {
        self.engine.get_token_count(text)
    }
}
