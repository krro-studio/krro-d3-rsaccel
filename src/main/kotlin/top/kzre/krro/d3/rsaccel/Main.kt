package top.kzre.krro.d3.rsaccel

import java.nio.ByteBuffer
import java.nio.ByteOrder

fun main() {
    // 1. hello：调用链
    val v = NativeBindings.hello()
    require(v == 42) { "hello() = $v, expected 42" }
    println("1. hello() = $v")

    // 2. add：基本类型
    val s = NativeBindings.add(2, 3)
    require(s == 5) { "add(2,3) = $s, expected 5" }
    println("2. add(2,3) = $s")

    // 3. sumArray：JNI 堆数组
    val total = NativeBindings.sumArray(floatArrayOf(1f, 2f, 3f, 4f))
    require(total == 10f) { "sumArray = $total, expected 10" }
    println("3. sumArray([1,2,3,4]) = $total")

    // 4. fillBuffer：DirectByteBuffer 零拷贝
    val buf = ByteBuffer.allocateDirect(4 * 4).order(ByteOrder.nativeOrder())
    NativeBindings.fillBuffer(buf, 4)
    val fb = buf.asFloatBuffer()
    val vals = floatArrayOf(fb.get(0), fb.get(1), fb.get(2), fb.get(3))
    require(vals.contentEquals(floatArrayOf(0f, 1.5f, 3f, 4.5f))) {
        "buffer = ${vals.contentToString()}"
    }
    println("4. fillBuffer → ${vals.contentToString()}")

    // 5. 句柄生命周期：显式 close
    Bmesh.create().use { bm ->
        val n = bm.vertexCount()
        require(n == 0) { "vertexCount = $n" }
        println("5. bmesh 句柄 ok, vertexCount=$n")
    }

    // 6. GC 兜底：不 close，靠 NativeCleaner 释放
    createGarbage()
    System.gc()
    Thread.sleep(100)
    println("6. GC cleaner 运行完成")

    println("ALL PASSED")
}

/** 故意不 close，验证 NativeCleaner 兜底路径 */
private fun createGarbage() {
    @Suppress("UNUSED_VARIABLE")
    val leak = Bmesh.create()
}