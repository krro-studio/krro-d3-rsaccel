package top.kzre.krro.d3.rsaccel

/**
 * BMesh 外壳，持有 Rust 侧句柄。
 *
 * 生命周期：显式 close() 立即释放；忘记 close() 时由 NativeCleaner 在 GC 后兜底。
 * 两种路径都保证 Rust 侧释放恰好一次。
 */
class Bmesh internal constructor(nativeHandle: Long) : AutoCloseable {

    @Volatile private var handle: Long = nativeHandle
    private val registration: NativeCleaner.Registration

    init {
        // 关键：捕获 handle 值快照，绝不捕获 this（否则 Cleaner 强引用外壳，永不回收）
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
            NativeBindings.bmeshRelease(h)
            registration.clean()   // 取消 Cleaner 注册，防二次释放
        }
    }

    companion object {
        @JvmStatic fun create(): Bmesh = Bmesh(NativeBindings.bmeshNew())
    }
}