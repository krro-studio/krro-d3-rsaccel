package top.kzre.krro.d3.rsaccel

import java.nio.ByteBuffer

/**
 * JNI 声明入口。
 *
 * 使用 `object + @JvmStatic` 而非顶层函数：需要 `init` 块加载 native 库，
 * `@JvmStatic` 保证 JNI 符号可预测（静态方法，接收 JClass）。
 *
 * Rust 侧对应符号：Java_top_kzre_krro_d3_rsaccel_NativeBindings_<方法名>
 */
internal object NativeBindings {
    init {
        org.questdb.JarJni.load("krro-d3-rsaccel")
    }

    @JvmStatic external fun hello(): Int
    @JvmStatic external fun add(a: Int, b: Int): Int
    @JvmStatic external fun sumArray(arr: FloatArray): Float
    @JvmStatic external fun fillBuffer(buffer: ByteBuffer, count: Int)

    @JvmStatic external fun bmeshNew(): Long
    @JvmStatic external fun bmeshRelease(handle: Long)
    @JvmStatic external fun bmeshVertexCount(handle: Long): Int
}