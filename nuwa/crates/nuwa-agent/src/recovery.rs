use crate::{Step, StepStatus, AgentError};
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum RecoveryStrategy {
    Retry,
    UseFallback,
    SkipStep,
    Abort,
    UserConfirm,
}

pub struct Recovery;

impl Recovery {
    pub fn new() -> Self {
        Self
    }

    pub fn get_strategy(&self, failed_step: &Step, error: &str) -> Result<RecoveryStrategy, AgentError> {
        if failed_step.fallback.is_some() {
            return Ok(RecoveryStrategy::UseFallback);
        }

        if error.contains("timeout") {
            return Ok(RecoveryStrategy::Retry);
        }

        if error.contains("element not found") {
            return Ok(RecoveryStrategy::SkipStep);
        }

        Ok(RecoveryStrategy::UserConfirm)
    }
}

impl Default for Recovery {
    fn default() -> Self {
        Self::new()
    }
}
