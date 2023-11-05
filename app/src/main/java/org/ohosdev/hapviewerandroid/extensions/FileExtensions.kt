package org.ohosdev.hapviewerandroid.extensions

import android.content.Context
import android.os.Build
import android.os.FileUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.channels.FileChannel

fun FileChannel.copyTo(out: FileChannel) {
    val size = this.size()
    var left = size
    while (left > 0) {
        left -= this.transferTo(size - left, left, out)
    }
}

/**
 * 复制文件。
 *
 * Android 10 以上使用系统自带的方法，速度会有所提升。
 * */
fun FileInputStream.copyTo(out: FileOutputStream) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        FileUtils.copy(this.fd, out.fd)
    }
    channel.use { inputChannel ->
        out.channel.use { outputChannel ->
            inputChannel.copyTo(
                outputChannel
            )
        }
    }
}

fun InputStream.copyTo(out: OutputStream) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        FileUtils.copy(this, out)
    } else {
        val b = ByteArray(1024 * 5)
        var len: Int
        while (read(b).also { len = it } != -1) {
            out.write(b, 0, len)
        }
        out.flush()
    }

}

fun File.copyTo(out: File) {
    FileInputStream(this).use { inputStream ->
        FileOutputStream(out).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }
}

fun File.isExternalCache(context: Context): Boolean {
    return absolutePath.run {
        context.externalCacheDir?.let { startsWith(it.absolutePath) } ?: false
                || startsWith(context.cacheDir)
    }
}


fun File.deleteIfCache(context: Context) {
    if (isExternalCache(context)) {
        if (!delete()) {
            deleteOnExit()
        }
    }
}