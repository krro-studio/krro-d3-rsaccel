package top.kzre.krro.d3.rsaccel

import java.lang.ref.PhantomReference
import java.lang.ref.ReferenceQueue
import java.util.concurrent.ConcurrentHashMap

/**
 * JDK 8 的 `java.lang.ref.Cleaner` 替代品。
 *
 * JDK 9+ 有官方 Cleaner；JDK 8 只有 PhantomReference + ReferenceQueue。
 * 语义等价：对象被 GC 判定不可达时，在后台线程执行清理任务。
 *
 * 关键约束：清理任务**不得捕获被清理对象本身**，否则对象永不可达 → 泄漏。
 * 调用方应捕获 handle 值快照，而非 `this`。
 */
internal class NativeCleaner private constructor() {

    private val queue = ReferenceQueue<Any>()
    private val tasks = ConcurrentHashMap<PhantomReference<*>, () -> Unit>()

    init {
        val thread = Thread {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    val ref = queue.remove() as PhantomReference<*>
                    tasks.remove(ref)?.invoke()
                    ref.clear()
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                    break
                }
            }
        }
        thread.isDaemon = true
        thread.name = "krro-d3-rsaccel-cleaner"
        thread.start()
    }

    fun register(obj: Any, cleanup: () -> Unit): Registration {
        val ref = PhantomReference(obj, queue)
        tasks[ref] = cleanup
        return Registration(ref)
    }

    inner class Registration internal constructor(
        private val ref: PhantomReference<*>
    ) {
        /** 显式清理并取消注册。close() 调用它，防止二次清理。 */
        fun clean() {
            tasks.remove(ref)?.invoke()
            ref.clear()
        }
    }

    companion object {
        val INSTANCE = NativeCleaner()
    }
}