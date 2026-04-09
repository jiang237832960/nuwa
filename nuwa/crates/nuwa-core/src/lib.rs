pub use nuwa_kernel::NuwaKernel;
pub use nuwa_agent::Agent;
pub use nuwa_world::WorldState;
pub use nuwa_self::SelfLayer;
pub use nuwa_memory::Memory;
pub use nuwa_forge::Forge;
pub use nuwa_growth::Growth;
pub use nuwa_inference::{InferenceEngine, NuwaInferenceEngine, ModelHandle, GGUFMeta, InferenceError};
pub use nuwa_tools::Tools;
pub use nuwa_audit::Audit;
pub use nuwa_device::Device;
pub use nuwa_benchmark_cn::BenchmarkCN;
pub use nuwa_copy::NegotiationCopy;

pub struct NuwaCore {
    kernel: NuwaKernel,
    agent: Agent,
    world: WorldState,
    self_layer: SelfLayer,
    memory: Memory,
    forge: Forge,
    growth: Growth,
    inference: NuwaInferenceEngine,
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
            inference: NuwaInferenceEngine::new(),
            tools: Tools::new(),
            audit: Audit::new(),
            device: Device::new(),
            benchmark_cn: BenchmarkCN::new(),
            copy: NegotiationCopy::new(),
        }
    }
}
