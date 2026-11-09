package top.kzre.krro.d3.rsaccel

class Bmesh internal constructor(nativeHandle: Long) : AutoCloseable {

    @Volatile private var handle: Long = nativeHandle
    private val registration: NativeCleaner.Registration

    init {
        // 捕获 handle 值快照，不捕获 this（否则 Cleaner 强引用外壳，永不回收）
        val h = nativeHandle
        registration = NativeCleaner.INSTANCE.register(this) {
            if (h != 0L) NativeBindings.bmeshRelease(h)
        }
    }

    fun vertexCount(): Int {
        val h = handle
        check(h != 0L) { "Bmesh already closed" }
        return NativeBindings.bmeshVertexCount(h)
    }

    override fun close() {
        val h = handle
        if (h != 0L) {
            handle = 0L
            // 只走 registration.clean()：它执行 cleanup lambda 释放 native handle，
            // 并从 tasks 中移除注册。绝不能在这里再手动 bmeshRelease，
            // 否则 lambda 会再释放一次 → 双重 free → 堆损坏。
            registration.clean()
        }
    }

    companion object {
        @JvmStatic fun create(): Bmesh = Bmesh(NativeBindings.bmeshNew())
    }
}