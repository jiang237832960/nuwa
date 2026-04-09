use serde::{Deserialize, Serialize};
use crate::{Task, Step, StepResult, StepStatus};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ExecutionReport {
    pub task_id: String,
    pub total_steps: usize,
    pub completed_steps: usize,
    pub failed_steps: usize,
    pub step_results: Vec<StepResult>,
    pub success: bool,
}

pub struct Executor;

impl Executor {
    pub fn new() -> Self {
        Self
    }

    pub fn execute(&self, task: &Task) -> Result<ExecutionReport, crate::AgentError> {
        let mut step_results = Vec::new();
        let mut completed = 0;
        let mut failed = 0;

        for step in &task.steps {
            let result = self.execute_step(step);
            step_results.push(result.clone());

            match result.status {
                StepStatus::Success => completed += 1,
                StepStatus::Failed => failed += 1,
                _ => {}
            }
        }

        Ok(ExecutionReport {
            task_id: task.id.clone(),
            total_steps: task.steps.len(),
            completed_steps: completed,
            failed_steps: failed,
            step_results,
            success: failed == 0,
        })
    }

    fn execute_step(&self, step: &Step) -> StepResult {
        StepResult {
            step_id: step.id,
            status: StepStatus::Success,
            message: None,
            screenshot: None,
        }
    }
}

impl Default for Executor {
    fn default() -> Self {
        Self::new()
    }
}
