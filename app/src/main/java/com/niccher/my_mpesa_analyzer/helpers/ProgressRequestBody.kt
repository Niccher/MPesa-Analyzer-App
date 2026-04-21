package com.niccher.my_mpesa_analyzer.helpers

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.Okio
import okio.Sink
import java.io.File
import java.io.IOException

class ProgressRequestBody(
    private val mFile: File,
    private val contentType: MediaType?,
    private val mListener: (progress: Int) -> Unit
) : RequestBody() {

    override fun contentType(): MediaType? {
        return contentType
    }

    override fun contentLength(): Long {
        return mFile.length()
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val fileLength = mFile.length()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val inStream = mFile.inputStream()
        var uploaded: Long = 0

        inStream.use { input ->
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                uploaded += read.toLong()
                sink.write(buffer, 0, read)
                val progress = ((uploaded.toDouble() / fileLength.toDouble()) * 100).toInt()
                mListener(progress)
            }
        }
    }
}
