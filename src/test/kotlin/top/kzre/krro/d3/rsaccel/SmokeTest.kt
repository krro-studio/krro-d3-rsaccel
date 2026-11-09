package top.kzre.krro.d3.rsaccel

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SmokeTest {

    @Test fun hello() = assertEquals(42, NativeBindings.hello())

    @Test fun add() = assertEquals(5, NativeBindings.add(2, 3))

    @Test fun sumArray() =
        assertEquals(10f, NativeBindings.sumArray(floatArrayOf(1f, 2f, 3f, 4f)))

    @Test fun fillBuffer() {
        val buf = ByteBuffer.allocateDirect(4 * 4).order(ByteOrder.nativeOrder())
        NativeBindings.fillBuffer(buf, 4)
        val fb = buf.asFloatBuffer()
        assertEquals(0f, fb.get(0))
        assertEquals(1.5f, fb.get(1))
        assertEquals(3f, fb.get(2))
        assertEquals(4.5f, fb.get(3))
    }

    @Test fun handleExplicitClose() {
        val bm = Bmesh.create()
        assertEquals(0, bm.vertexCount())
        bm.close()
        // 二次 close 是幂等的
        bm.close()
    }

    @Test fun handleClosedThrows() {
        val bm = Bmesh.create()
        bm.close()
        var threw = false
        try { bm.vertexCount() } catch (_: IllegalStateException) { threw = true }
        assertTrue(threw, "vertexCount after close should throw")
    }
}