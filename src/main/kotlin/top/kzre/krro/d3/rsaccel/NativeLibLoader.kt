package top.kzre.krro.d3.rsaccel

import io.questdb.jar.jni.JarJniLoader

object NativeLibLoader {

    private const val CUSTOM_PATH_PROP = "krro.library.classpath"

    fun load(libName: String) {
        val clazz = NativeLibLoader::class.java

        // 候选资源根前缀（jarPathPrefix），不是完整路径
        val prefixes = buildList {
            System.getProperty(CUSTOM_PATH_PROP)?.takeIf { it.isNotBlank() }?.let { custom ->
                add(if (custom.startsWith("/")) custom else "/$custom")
            }
            add("/")                 // 平台目录直接位于 JAR 根下：/win32-x86-64/...
            add("/native")           // 回退：/native/win32-x86-64/...
        }

        val failures = StringBuilder()
        for (prefix in prefixes) {
            try {
                // 3 参数版本：内部自动添加平台目录和文件扩展名
                JarJniLoader.loadLib(clazz, prefix, libName)
                println("[NativeLibLoader] loaded via prefix: $prefix")
                return
            } catch (e: Throwable) {
                failures.append("\n  - prefix '").append(prefix).append("' : ").append(e.message)
            }
        }

        throw UnsatisfiedLinkError(
            "Cannot load native library '$libName'. Tried prefixes:$failures"
        )
    }
}