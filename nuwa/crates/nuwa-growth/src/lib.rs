use serde::{Deserialize, Serialize};
use std::time::{SystemTime, UNIX_EPOCH};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Growth {
    budget: GrowthBudget,
    experience_count: usize,
    workflow_count: usize,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct GrowthBudget {
    daily_learning_time_minutes: u32,
    weekly_distill_count: u32,
    storage_budget_mb: u64,
    battery_threshold: f32,
    last_reset: u64,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct BudgetInfo {
    pub daily_learning_remaining: u32,
    pub weekly_distill_remaining: u32,
    pub storage_used_mb: u64,
    pub storage_budget_mb: u64,
    pub can_learn_on_battery: bool,
}

impl Growth {
    pub fn new() -> Self {
        Self {
            budget: GrowthBudget {
                daily_learning_time_minutes: 60,
                weekly_distill_count: 10,
                storage_budget_mb: 1024,
                battery_threshold: 0.3,
                last_reset: SystemTime::now()
                    .duration_since(UNIX_EPOCH)
                    .unwrap()
                    .as_secs(),
            },
            experience_count: 0,
            workflow_count: 0,
        }
    }

    pub fn get_budget_info(&self) -> BudgetInfo {
        BudgetInfo {
            daily_learning_remaining: self.budget.daily_learning_time_minutes,
            weekly_distill_remaining: self.budget.weekly_distill_count,
            storage_used_mb: 0,
            storage_budget_mb: self.budget.storage_budget_mb,
            can_learn_on_battery: true,
        }
    }

    pub fn request_growth(&mut self, _action: &str) -> Result<(), &'static str> {
        let budget = self.get_budget_info();
        if budget.daily_learning_remaining > 0 {
            Ok(())
        } else {
            Err("Budget exhausted")
        }
    }
}

impl Default for Growth {
    fn default() -> Self {
        Self::new()
    }
}
