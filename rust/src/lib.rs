//! krro-d3-rsaccel: Rust 加速内核（JVM 通过 JNI 调用）
//!
//! 内部逻辑与 JNI 边界分离：`jni` 模块只做类型转换和跨语言交接，
//! 真正实现放在独立模块中，可独立 `cargo test`。

mod jni;