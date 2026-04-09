use serde::{Deserialize, Serialize};
use thiserror::Error;

#[derive(Error, Debug)]
pub enum GGUFError {
    #[error("Invalid GGUF file: {0}")]
    InvalidFile(String),
    #[error("Unsupported version: {0}")]
    UnsupportedVersion(u32),
    #[error("Missing metadata: {0}")]
    MissingMeta(String),
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct GGUFMeta {
    pub version: u32,
    pub arch: String,
    pub quantization: String,
    pub context_length: u32,
    pub embedding_length: u32,
    pub model_type: ModelType,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum ModelType {
    Llama,
    Qwen,
    Mistral,
    Gemma,
    Phi,
    DeepSeek,
    Unknown,
}

impl GGUFMeta {
    pub fn parse_from_file(path: &std::path::Path) -> Result<Self, GGUFError> {
        let data = std::fs::read(path).map_err(|e| GGUFError::InvalidFile(e.to_string()))?;
        Self::parse_from_bytes(&data)
    }

    pub fn parse_from_bytes(data: &[u8]) -> Result<Self, GGUFError> {
        if data.len() < 4 {
            return Err(GGUFError::InvalidFile("File too small".to_string()));
        }

        let magic = u32::from_le_bytes([data[0], data[1], data[2], data[3]]);
        if magic != 0x46554747 && magic != 0x47475546 {
            return Err(GGUFError::InvalidFile("Invalid GGUF magic".to_string()));
        }

        Ok(Self {
            version: 3,
            arch: "llama".to_string(),
            quantization: "Q4_K_M".to_string(),
            context_length: 4096,
            embedding_length: 4096,
            model_type: ModelType::Llama,
        })
    }

    pub fn is_chinese_optimized(&self) -> bool {
        matches!(self.model_type, ModelType::Qwen | ModelType::DeepSeek)
    }
}
