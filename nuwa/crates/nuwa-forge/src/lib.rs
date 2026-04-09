pub struct Forge {
    workflows: Vec<WorkflowTemplate>,
}

#[derive(Debug, Clone)]
pub struct WorkflowTemplate {
    pub id: String,
    pub name: String,
    pub app: String,
    pub steps: Vec<crate::nuwa_agent::Step>,
}

impl Forge {
    pub fn new() -> Self {
        Self {
            workflows: Vec::new(),
        }
    }

    pub fn register_workflow(&mut self, template: WorkflowTemplate) {
        self.workflows.push(template);
    }

    pub fn find_workflow(&self, app: &str, action: &str) -> Option<&WorkflowTemplate> {
        self.workflows.iter().find(|w| w.app == app && w.name.contains(action))
    }
}

impl Default for Forge {
    fn default() -> Self {
        Self::new()
    }
}
