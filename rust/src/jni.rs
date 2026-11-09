use jni::JNIEnv;
use jni::objects::{JByteBuffer, JClass, JFloatArray};
use jni::sys::{jfloat, jint, jlong};
use std::sync::Arc;

// ==================================================================
// 阶段零：四类基础数据通路
// ==================================================================

/// 无参返回：验证调用链本身
#[no_mangle]
pub extern "system" fn Java_top_kzre_krro_d3_rsaccel_NativeBindings_hello(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    42
}

/// 基本类型参数：验证参数编组
#[no_mangle]
pub extern "system" fn Java_top_kzre_krro_d3_rsaccel_NativeBindings_add(
    _env: JNIEnv,
    _class: JClass,
    a: jint,
    b: jint,
) -> jint {
    a + b
}

/// JNI 堆数组：验证数组读写（有拷贝）
#[no_mangle]
pub extern "system" fn Java_top_kzre_krro_d3_rsaccel_NativeBindings_sumArray(
    mut env: JNIEnv,
    _class: JClass,
    arr: JFloatArray,
) -> jfloat {
    let len = match env.get_array_length(&arr) {
        Ok(n) => n,
        Err(_) => return 0.0,
    };
    let mut buf = vec![0f32; len as usize];
    if env.get_float_array_region(&arr, 0, &mut buf).is_err() {
        return 0.0;
    }
    buf.iter().sum()
}

/// DirectByteBuffer 零拷贝：Rust 直接写 Java 的 off-heap 内存
#[no_mangle]
pub extern "system" fn Java_top_kzre_krro_d3_rsaccel_NativeBindings_fillBuffer(
    env: JNIEnv,
    _class: JClass,
    buffer: JByteBuffer,
    count: jint,
) {
    let ptr = match env.get_direct_buffer_address(&buffer) {
        Ok(p) => p,
        Err(_) => return,
    };
    let len = match env.get_direct_buffer_capacity(&buffer) {
        Ok(l) => l,
        Err(_) => return,
    };
    // DirectByteBuffer 天然按 8 字节对齐，转 *mut f32 安全
    let floats = unsafe { std::slice::from_raw_parts_mut(ptr as *mut f32, len / 4) };
    let n = (count as usize).min(floats.len());
    for i in 0..n {
        floats[i] = (i as f32) * 1.5;
    }
}

// ==================================================================
// 阶段一：最小句柄生命周期
//
// handle 指向 *mut Arc<BmeshHandle>（Box 包裹的 Arc），保证地址稳定。
// 后续 Cow 语义依赖这个结构：Arc::make_mut 只改 Arc 内部指针，
// 不改 Arc 自身地址，所以 handle 永远有效。
// ==================================================================

struct BmeshHandle {
    vertex_count: usize,
}

#[no_mangle]
pub extern "system" fn Java_top_kzre_krro_d3_rsaccel_NativeBindings_bmeshNew(
    _env: JNIEnv,
    _class: JClass,
) -> jlong {
    let handle = BmeshHandle { vertex_count: 0 };
    Box::into_raw(Box::new(Arc::new(handle))) as jlong
}

#[no_mangle]
pub extern "system" fn Java_top_kzre_krro_d3_rsaccel_NativeBindings_bmeshRelease(
    _env: JNIEnv,
    _class: JClass,
    handle: jlong,
) {
    if handle == 0 {
        return;
    }
    // 与 bmeshNew 的 Box::into_raw 配对
    unsafe {
        drop(Box::from_raw(handle as *mut Arc<BmeshHandle>));
    }
}

#[no_mangle]
pub extern "system" fn Java_top_kzre_krro_d3_rsaccel_NativeBindings_bmeshVertexCount(
    _env: JNIEnv,
    _class: JClass,
    handle: jlong,
) -> jint {
    if handle == 0 {
        return 0;
    }
    let arc = unsafe { &*(handle as *const Arc<BmeshHandle>) };
    arc.vertex_count as jint
}