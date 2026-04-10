use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use thiserror::Error;

#[derive(Error, Debug)]
pub enum GGUFError {
    #[error("Invalid GGUF file: {0}")]
    InvalidFile(String),
    #[error("Unsupported version: {0}")]
    UnsupportedVersion(u32),
    #[error("Missing metadata: {0}")]
    MissingMeta(String),
    #[error("Parse error: {0}")]
    ParseError(String),
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct GGUFMeta {
    pub version: u32,
    pub arch: String,
    pub quantization: String,
    pub context_length: u32,
    pub embedding_length: u32,
    pub model_type: ModelType,
    pub tokenizer: TokenizerInfo,
    pub metadata: HashMap<String, String>,
    pub name: String,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum ModelType {
    Llama,
    Qwen,
    Qwen2,
    Mistral,
    Gemma,
    Phi,
    DeepSeek,
    ChatGLM,
    Baichuan,
    Unknown,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct TokenizerInfo {
    pub model: String,
    pub vocabulary_size: u32,
    pub added_tokens: u32,
}

impl Default for TokenizerInfo {
    fn default() -> Self {
        Self {
            model: "clip".to_string(),
            vocabulary_size: 32000,
            added_tokens: 0,
        }
    }
}

impl ModelType {
    pub fn detect(arch: &str) -> ModelType {
        match arch.to_lowercase().as_str() {
            s if s.contains("qwen2") || s.contains("qwen_2") => ModelType::Qwen2,
            s if s.contains("qwen") || s.contains("qianwen") => ModelType::Qwen,
            s if s.contains("llama") || s.contains("llama2") || s.contains("llama3") => ModelType::Llama,
            s if s.contains("mistral") || s.contains("mixtral") => ModelType::Mistral,
            s if s.contains("gemma") => ModelType::Gemma,
            s if s.contains("phi") => ModelType::Phi,
            s if s.contains("deepseek") || s.contains("deep_seek") => ModelType::DeepSeek,
            s if s.contains("chatglm") || s.contains("glm") => ModelType::ChatGLM,
            s if s.contains("baichuan") || s.contains("baichuan2") => ModelType::Baichuan,
            _ => ModelType::Unknown,
        }
    }
}

impl GGUFMeta {
    pub fn parse_from_file(path: &std::path::Path) -> Result<Self, GGUFError> {
        let data = std::fs::read(path).map_err(|e| GGUFError::InvalidFile(e.to_string()))?;
        Self::parse_from_bytes(&data)
    }

    pub fn parse_from_bytes(data: &[u8]) -> Result<Self, GGUFError> {
        if data.len() < 12 {
            return Err(GGUFError::InvalidFile("File too small".to_string()));
        }

        let magic = u32::from_le_bytes([data[0], data[1], data[2], data[3]]);
        let is_gguf = magic == 0x46554747 || magic == 0x47475546;
        let is_ggml = magic == 0x67676d6c || magic == 0x6c6d6767;

        if !is_gguf && !is_ggml {
            return Err(GGUFError::InvalidFile("Invalid GGUF/GGML magic number".to_string()));
        }

        if is_gguf {
            parse_gguf(data)
        } else {
            parse_ggml(data)
        }
    }

    pub fn is_chinese_optimized(&self) -> bool {
        matches!(self.model_type, 
            ModelType::Qwen | 
            ModelType::Qwen2 | 
            ModelType::DeepSeek | 
            ModelType::ChatGLM | 
            ModelType::Baichuan
        )
    }

    pub fn supports_function_calling(&self) -> bool {
        matches!(self.model_type, ModelType::Qwen2 | ModelType::DeepSeek)
    }
}

fn parse_gguf(data: &[u8]) -> Result<GGUFMeta, GGUFError> {
    if data.len() < 8 {
        return Err(GGUFError::InvalidFile("Invalid GGUF header".to_string()));
    }

    let version = u32::from_le_bytes([data[4], data[5], data[6], data[7]]);
    
    if version < 2 || version > 4 {
        return Err(GGUFError::UnsupportedVersion(version));
    }

    let mut offset = 8;
    let mut metadata = HashMap::new();
    
    while offset + 12 <= data.len() {
        let metadata_type = u32::from_le_bytes([data[offset], data[offset+1], data[offset+2], data[offset+3]]);
        let key_len = u32::from_le_bytes([data[offset+4], data[offset+5], data[offset+6], data[offset+7]]);
        offset += 8;
        
        if offset + key_len as usize > data.len() {
            break;
        }
        
        let key = String::from_utf8_lossy(&data[offset..offset+key_len as usize]).to_string();
        offset += key_len as usize;
        
        match metadata_type {
            0 => {
                if offset + 4 <= data.len() {
                    let value = i32::from_le_bytes([data[offset], data[offset+1], data[offset+2], data[offset+3]]);
                    metadata.insert(key, value.to_string());
                    offset += 4;
                }
            },
            1 => {
                if offset + 4 <= data.len() {
                    let value = f32::from_le_bytes([data[offset], data[offset+1], data[offset+2], data[offset+3]]);
                    metadata.insert(key, value.to_string());
                    offset += 4;
                }
            },
            2 => {
                if offset + 1 <= data.len() {
                    let value = data[offset];
                    metadata.insert(key, value.to_string());
                    offset += 1;
                }
            },
            3 => {
                if offset + 4 <= data.len() {
                    let value = u32::from_le_bytes([data[offset], data[offset+1], data[offset+2], data[offset+3]]);
                    metadata.insert(key, value.to_string());
                    offset += 4;
                }
            },
            4 | 5 | 6 | 7 => {
                if offset + 8 <= data.len() {
                    let value = u64::from_le_bytes([data[offset], data[offset+1], data[offset+2], data[offset+3], data[offset+4], data[offset+5], data[offset+6], data[offset+7]]);
                    metadata.insert(key, value.to_string());
                    offset += 8;
                }
            },
            8 => {
                if offset + 4 <= data.len() {
                    let str_len = u32::from_le_bytes([data[offset], data[offset+1], data[offset+2], data[offset+3]]) as usize;
                    offset += 4;
                    if offset + str_len <= data.len() {
                        let value = String::from_utf8_lossy(&data[offset..offset+str_len]).to_string();
                        metadata.insert(key, value);
                        offset += str_len;
                    }
                }
            },
            _ => break,
        }
    }

    let arch = metadata.get("general.architecture")
        .or_else(|| metadata.get("model.architecture"))
        .cloned()
        .unwrap_or_else(|| "llama".to_string());

    let model_type = ModelType::detect(&arch);
    let quantization = metadata.get("general.quantization_version")
        .map(|q| format!("Q{}", q))
        .unwrap_or_else(|| "Q4_K_M".to_string());

    let context_length = metadata
        .get("llama.context_length")
        .or_else(|| metadata.get("qwen.context_length"))
        .or_else(|| metadata.get("context_length"))
        .and_then(|s| s.parse().ok())
        .unwrap_or(4096);

    let embedding_length = metadata
        .get("llama.embedding_length")
        .or_else(|| metadata.get("qwen.embedding_length"))
        .or_else(|| metadata.get("embedding_length"))
        .and_then(|s| s.parse().ok())
        .unwrap_or(4096);

    let vocab_size = metadata
        .get("llama.vocab_size")
        .or_else(|| metadata.get("qwen.vocab_size"))
        .or_else(|| metadata.get("vocab_size"))
        .and_then(|s| s.parse().ok())
        .unwrap_or(32000);

    Ok(GGUFMeta {
        version,
        arch,
        quantization,
        context_length,
        embedding_length,
        model_type,
        tokenizer: TokenizerInfo {
            model: "tiktoken".to_string(),
            vocabulary_size: vocab_size,
            added_tokens: 0,
        },
        metadata,
        name: "Imported Model".to_string(),
    })
}

fn parse_ggml(data: &[u8]) -> Result<GGUFMeta, GGUFError> {
    Ok(GGUFMeta {
        version: 1,
        arch: "llama".to_string(),
        quantization: "Q4_K_M".to_string(),
        context_length: 2048,
        embedding_length: 4096,
        model_type: ModelType::Llama,
        tokenizer: TokenizerInfo::default(),
        metadata: HashMap::new(),
        name: "GGML Model".to_string(),
    })
}
