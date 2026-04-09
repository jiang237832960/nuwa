use super::{InferenceEngine, InferenceError, ModelHandle, GGUFMeta};
use thiserror::Error;

#[derive(Error, Debug)]
pub enum LlamaError {
    #[error("LLM runtime error: {0}")]
    RuntimeError(String),
    #[error("Context creation failed: {0}")]
    ContextError(String),
}

pub struct LlamaEngine {
    context_size: usize,
    gpu_layers: i32,
}

impl LlamaEngine {
    pub fn new() -> Self {
        Self {
            context_size: 4096,
            gpu_layers: 0,
        }
    }

    pub fn with_context_size(mut self, size: usize) -> Self {
        self.context_size = size;
        self
    }

    pub fn with_gpu_layers(mut self, layers: i32) -> Self {
        self.gpu_layers = layers;
        self
    }
}

impl Default for LlamaEngine {
    fn default() -> Self {
        Self::new()
    }
}

impl InferenceEngine for LlamaEngine {
    fn load_model(&mut self, path: &std::path::Path) -> Result<ModelHandle, InferenceError> {
        let meta = GGUFMeta::parse_from_file(path)
            .map_err(|e| InferenceError::LoadError(e.to_string()))?;

        Ok(ModelHandle {
            id: 0,
            path: path.to_string_lossy().to_string(),
            meta,
        })
    }

    fn generate(&self, _handle: &ModelHandle, prompt: &str) -> Result<String, InferenceError> {
        Ok(format!("[模拟推理] {}", prompt))
    }

    fn get_token_count(&self, text: &str) -> usize {
        text.chars().count() / 2
    }
}
