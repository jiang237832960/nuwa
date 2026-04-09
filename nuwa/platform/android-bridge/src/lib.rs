#![allow(non_snake_case)]

use jni::JNIEnv;
use jni::objects::{JClass, JString, JValue};
use jni::sys::{jboolean, jlong, jstring};
use std::sync::Mutex;
use nuwa_inference::{InferenceEngine, LlamaEngine, ModelHandle};
use nuwa_world::{WorldStateManager, DeviceResources};
use nuwa_kernel::NuwaKernel;

static ENGINE: Mutex<Option<LlamaEngine>> = Mutex::new(None);
static MODEL_HANDLE: Mutex<Option<ModelHandle>> = Mutex::new(None);
static WORLD_STATE: WorldStateManager = WorldStateManager::new();
static KERNEL: NuwaKernel = NuwaKernel::new();

#[no_mangle]
pub extern "C" fn Java_ai_nuwa_app_bridge_RustBridge_initialize(
    _env: JNIEnv,
    _class: JClass,
) -> jboolean {
    let mut engine = ENGINE.lock().unwrap();
    *engine = Some(LlamaEngine::new());
    log::info!("Nuwa Rust Bridge initialized");
    true as jboolean
}

#[no_mangle]
pub extern "C" fn Java_ai_nuwa_app_bridge_RustBridge_loadModel(
    env: JNIEnv,
    _class: JClass,
    path: JString,
) -> jlong {
    let path_str: String = env.get_string(path)
        .expect("Couldn't get Java string!")
        .into();
    
    let mut engine_guard = ENGINE.lock().unwrap();
    if let Some(ref mut engine) = *engine_guard {
        match engine.load_model(std::path::Path::new(&path_str)) {
            Ok(handle) => {
                let mut model_guard = MODEL_HANDLE.lock().unwrap();
                let id = model_guard.len() as u64 + 1;
                *model_guard = Some(handle);
                log::info!("Model loaded successfully with id: {}", id);
                id as jlong
            }
            Err(e) => {
                log::error!("Failed to load model: {}", e);
                -1 as jlong
            }
        }
    } else {
        -1 as jlong
    }
}

#[no_mangle]
pub extern "C" fn Java_ai_nuwa_app_bridge_RustBridge_generate(
    env: JNIEnv,
    _class: JClass,
    model_id: jlong,
    prompt: JString,
) -> jstring {
    let prompt_str: String = env.get_string(prompt)
        .expect("Couldn't get Java string!")
        .into();
    
    let model_guard = MODEL_HANDLE.lock().unwrap();
    if let Some(ref handle) = *model_guard {
        let engine_guard = ENGINE.lock().unwrap();
        if let Some(ref engine) = *engine_guard {
            match engine.generate(handle, &prompt_str) {
                Ok(result) => {
                    let result_jstring = env.new_string(&result)
                        .expect("Couldn't create Java string!");
                    return result_jstring.into_raw();
                }
                Err(e) => {
                    log::error!("Generation failed: {}", e);
                }
            }
        }
    }
    JString::from(env.new_string("").expect("Couldn't create empty string")).into_raw()
}

#[no_mangle]
pub extern "C" fn Java_ai_nuwa_app_bridge_RustBridge_getResources(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let resources = WORLD_STATE.get().device_resources;
    let json = serde_json::to_string(&resources).unwrap_or_default();
    env.new_string(&json)
        .expect("Couldn't create Java string!")
        .into_raw()
}

#[no_mangle]
pub extern "C" fn Java_ai_nuwa_app_bridge_RustBridge_getForegroundApp(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let foreground = WORLD_STATE.get().foreground_app.clone();
    env.new_string(&foreground)
        .expect("Couldn't create Java string!")
        .into_raw()
}
