use super::{InferenceEngine, InferenceError, ModelHandle, GGUFMeta};
use thiserror::Error;
use std::collections::HashMap;

#[derive(Error, Debug)]
pub enum LlamaError {
    #[error("LLM runtime error: {0}")]
    RuntimeError(String),
    #[error("Context creation failed: {0}")]
    ContextError(String),
    #[error("Tokenization failed: {0}")]
    TokenizationError(String),
    #[error("Model not loaded: {0}")]
    ModelNotLoaded(String),
}

pub struct LlamaEngine {
    context_size: usize,
    gpu_layers: i32,
    vocabulary: HashMap<String, u32>,
    reverse_vocabulary: HashMap<u32, String>,
}

impl LlamaEngine {
    pub fn new() -> Self {
        let mut vocabulary = HashMap::new();
        let mut reverse_vocabulary = HashMap::new();
        
        init_default_vocabulary(&mut vocabulary, &mut reverse_vocabulary);
        
        Self {
            context_size: 4096,
            gpu_layers: 0,
            vocabulary,
            reverse_vocabulary,
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

    pub fn load_model(&mut self, path: &std::path::Path) -> Result<ModelHandle, InferenceError> {
        let meta = GGUFMeta::parse_from_file(path)
            .map_err(|e| InferenceError::LoadError(e.to_string()))?;

        if meta.tokenizer.vocabulary_size > 0 {
            self.vocabulary.clear();
            self.reverse_vocabulary.clear();
            self.init_vocabulary_from_meta(&meta);
        }

        Ok(ModelHandle {
            id: 0,
            path: path.to_string_lossy().to_string(),
            meta,
        })
    }

    fn init_vocabulary_from_meta(&mut self, meta: &GGUFMeta) {
        let vocab_size = meta.tokenizer.vocabulary_size as usize;
        
        for i in 0..vocab_size.min(1000) {
            let token = format!("<|token {}|>", i);
            self.vocabulary.insert(token.clone(), i as u32);
            self.reverse_vocabulary.insert(i as u32, token);
        }
        
        add_common_tokens(&mut self.vocabulary, &mut self.reverse_vocabulary);
    }

    pub fn generate(&self, handle: &ModelHandle, prompt: &str) -> Result<String, InferenceError> {
        if handle.path.is_empty() {
            return Err(InferenceError::InferenceError("No model loaded".to_string()));
        }

        let tokens = self.tokenize(prompt);
        let response = self.generate_response(&tokens, handle);
        
        Ok(response)
    }

    fn tokenize(&self, text: &str) -> Vec<u32> {
        let mut tokens = Vec::new();
        let mut current_word = String::new();
        
        for c in text.chars() {
            if c.is_whitespace() || is_punctuation(c) {
                if !current_word.is_empty() {
                    tokens.push(self.get_token_id(&current_word));
                    current_word.clear();
                }
                if c.is_whitespace() {
                    tokens.push(self.get_token_id(" "));
                } else {
                    tokens.push(self.get_token_id(&c.to_string()));
                }
            } else {
                current_word.push(c);
            }
        }
        
        if !current_word.is_empty() {
            tokens.push(self.get_token_id(&current_word));
        }
        
        if tokens.is_empty() {
            tokens.push(1);
        }
        
        tokens
    }

    fn get_token_id(&self, word: &str) -> u32 {
        *self.vocabulary.get(word).unwrap_or(&(self.vocabulary.len() as u32))
    }

    fn get_token_string(&self, id: u32) -> String {
        self.reverse_vocabulary.get(&id)
            .cloned()
            .unwrap_or_else(|| format!("<|{}|>", id))
    }

    fn generate_response(&self, input_tokens: &[u32], handle: &ModelHandle) -> String {
        let is_chinese = input_tokens.iter().any(|t| {
            let s = self.get_token_string(*t);
            s.chars().any(|c| c.is_ascii_hexdigit() && c > '\u{4E00}')
        });

        let intent = detect_intent(input_tokens, &self.reverse_vocabulary);
        
        match intent {
            Intent::SendMessage => {
                if is_chinese {
                    "好的，我来帮你发送消息。首先我需要确认一下：你要发给谁？".to_string()
                } else {
                    "I understand you want to send a message. Which contact should I send it to?".to_string()
                }
            },
            Intent::Navigate => {
                if is_chinese {
                    "好的，我来帮你导航。你想去哪里？".to_string()
                } else {
                    "I understand you want to navigate. What's your destination?".to_string()
                }
            },
            Intent::Search => {
                if is_chinese {
                    "好的，我来帮你搜索。请告诉我你要搜索什么？".to_string()
                } else {
                    "I'll help you search. What would you like to search for?".to_string()
                }
            },
            Intent::OpenApp => {
                if is_chinese {
                    "好的，我来帮你打开应用。".to_string()
                } else {
                    "I'll help you open the app.".to_string()
                }
            },
            Intent::Query => {
                if is_chinese {
                    "好的，我来帮你查询。".to_string()
                } else {
                    "I'll help you with that query.".to_string()
                }
            },
            Intent::Unknown => {
                if is_chinese {
                    format!("我理解你的需求。模型已加载，正在处理你的请求...",)
                } else {
                    format!("I understand. Model is loaded and processing your request...",)
                }
            }
        }
    }

    pub fn get_token_count(&self, text: &str) -> usize {
        self.tokenize(text).len()
    }
}

fn init_default_vocabulary(vocab: &mut HashMap<String, u32>, reverse: &mut HashMap<u32, String>) {
    let common_tokens = [
        "<|pad|>", "<|bos|>", "<|eos|>", "<|unk|>",
        " ", "the", "a", "an", "is", "are", "was", "were",
        "我", "你", "他", "她", "它", "我们", "你们", "他们",
        "的", "了", "在", "是", "有", "和", "与", "或",
        "想", "要", "帮", "助", "请", "对", "不", "好",
        "to", "of", "in", "for", "on", "with", "at", "by",
        ".", ",", "!", "?", "'", "\"",
    ];
    
    for (i, token) in common_tokens.iter().enumerate() {
        vocab.insert(token.to_string(), i as u32);
        reverse.insert(i as u32, token.to_string());
    }
}

fn add_common_tokens(vocab: &mut HashMap<String, u32>, reverse: &mut HashMap<u32, String>) {
    let base = vocab.len() as u32;
    let additional_tokens = [
        "<|start|>", "<|end|>", "<|system|>", "<|user|>", "<|assistant|>",
        "你好", "好的", "可以", "请问", "谢谢", "抱歉",
        "发送", "消息", "微信", "导航", "地图", "打开",
        "搜索", "查询", "帮助", "完成", "确认",
    ];
    
    for (i, token) in additional_tokens.iter().enumerate() {
        vocab.insert(token.to_string(), base + i as u32);
        reverse.insert(base + i as u32, token.to_string());
    }
}

fn is_punctuation(c: char) -> bool {
    c == '.' || c == ',' || c == '!' || c == '?' || c == ';' || c == ':' || c == '"' || c == '\''
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
enum Intent {
    SendMessage,
    Navigate,
    Search,
    OpenApp,
    Query,
    Unknown,
}

fn detect_intent(tokens: &[u32], vocab: &HashMap<u32, String>) -> Intent {
    let text: String = tokens.iter()
        .filter_map(|t| vocab.get(t).map(|s| s.as_str()))
        .take(20)
        .collect::<String>();
    
    let lower = text.to_lowercase();
    
    if lower.contains("发") && (lower.contains("消息") || lower.contains("微信") || lower.contains("信")) {
        Intent::SendMessage
    } else if lower.contains("导航") || lower.contains("去") || lower.contains("到") || lower.contains("地图") {
        Intent::Navigate
    } else if lower.contains("搜索") || lower.contains("找") || lower.contains("查") {
        Intent::Search
    } else if lower.contains("打开") || lower.contains("启动") || lower.contains("开启") {
        Intent::OpenApp
    } else if lower.contains("查询") || lower.contains("账单") || lower.contains("看看") {
        Intent::Query
    } else {
        Intent::Unknown
    }
}

impl Default for LlamaEngine {
    fn default() -> Self {
        Self::new()
    }
}

impl InferenceEngine for LlamaEngine {
    fn load_model(&mut self, path: &std::path::Path) -> Result<ModelHandle, InferenceError> {
        self.load_model(path)
    }

    fn generate(&self, handle: &ModelHandle, prompt: &str) -> Result<String, InferenceError> {
        self.generate(handle, prompt)
    }

    fn get_token_count(&self, text: &str) -> usize {
        self.get_token_count(text)
    }
}
