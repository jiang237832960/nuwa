mod intent;
mod planner;
mod executor;
mod recovery;

pub use intent::{IntentParser, Intent, IntentError};
pub use planner::{TaskPlanner, Task, Step, StepResult, StepStatus, Action, Target, Selector, SelectorType};
pub use executor::{Executor, ExecutionReport};
pub use recovery::{Recovery, RecoveryStrategy};

use thiserror::Error;

#[derive(Error, Debug)]
pub enum AgentError {
    #[error("Intent parsing failed: {0}")]
    ParseError(String),
    #[error("Planning failed: {0}")]
    PlanError(String),
    #[error("Execution failed: {0}")]
    ExecutionError(String),
    #[error("Recovery failed: {0}")]
    RecoveryError(String),
}

pub struct Agent {
    intent_parser: IntentParser,
    planner: TaskPlanner,
    executor: Executor,
    recovery: Recovery,
}

impl Agent {
    pub fn new() -> Self {
        Self {
            intent_parser: IntentParser::new(),
            planner: TaskPlanner::new(),
            executor: Executor::new(),
            recovery: Recovery::new(),
        }
    }

    pub fn parse_intent(&self, input: &str) -> Result<Intent, AgentError> {
        self.intent_parser.parse(input).map_err(|e| AgentError::ParseError(e.to_string()))
    }

    pub fn plan(&self, intent: &Intent) -> Result<Task, AgentError> {
        self.planner.plan(intent)
    }

    pub fn execute(&self, task: &Task) -> Result<ExecutionReport, AgentError> {
        self.executor.execute(task)
    }

    pub fn recover(&self, failed_step: &Step, error: &str) -> Result<RecoveryStrategy, AgentError> {
        self.recovery.get_strategy(failed_step, error)
    }
}
