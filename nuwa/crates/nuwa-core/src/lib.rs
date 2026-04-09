pub mod kernel;
pub mod agent;
pub mod world;
pub mod self_layer;
pub mod memory;
pub mod forge;
pub mod growth;
pub mod inference;
pub mod tools;
pub mod audit;
pub mod device;
pub mod benchmark_cn;
pub mod copy;

pub use kernel::NuwaKernel;
pub use agent::Agent;
pub use world::WorldState;
pub use self_layer::SelfLayer;
pub use memory::Memory;
pub use forge::Forge;
pub use growth::Growth;
pub use inference::InferenceEngine;
pub use tools::Tools;
pub use audit::Audit;
pub use device::Device;
pub use benchmark_cn::BenchmarkCN;
pub use copy::NegotiationCopy;

pub struct NuwaCore {
    kernel: NuwaKernel,
    agent: Agent,
    world: WorldState,
    self_layer: SelfLayer,
    memory: Memory,
    forge: Forge,
    growth: Growth,
    inference: InferenceEngine,
    tools: Tools,
    audit: Audit,
    device: Device,
    benchmark_cn: BenchmarkCN,
    copy: NegotiationCopy,
}

impl NuwaCore {
    pub fn new() -> Self {
        Self {
            kernel: NuwaKernel::new(),
            agent: Agent::new(),
            world: WorldState::new(),
            self_layer: SelfLayer::new(),
            memory: Memory::new(),
            forge: Forge::new(),
            growth: Growth::new(),
            inference: InferenceEngine::new(),
            tools: Tools::new(),
            audit: Audit::new(),
            device: Device::new(),
            benchmark_cn: BenchmarkCN::new(),
            copy: NegotiationCopy::new(),
        }
    }
}
