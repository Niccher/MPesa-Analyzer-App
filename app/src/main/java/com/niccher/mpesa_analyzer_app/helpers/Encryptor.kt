package com.niccher.mpesa_analyzer_app.helpers

import com.niccher.mpesa_analyzer_app.constants.Constants
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class Encryptor {
    companion object {
        private val kon = Constants

        @Throws(
            NoSuchAlgorithmException::class,
            NoSuchPaddingException::class,
            InvalidAlgorithmParameterException::class,
            InvalidKeyException::class,
            IOException::class
        )
        fun encodeToFile(keyStr: String, input: InputStream, output: OutputStream) {
            try {
                val ivBytes = ByteArray(16)
                java.security.SecureRandom().nextBytes(ivBytes)
                val iv = IvParameterSpec(ivBytes)
                val keySpec = SecretKeySpec(keyStr.toByteArray(StandardCharsets.UTF_8), kon.STRING_ALGO)

                output.write(ivBytes)

                val cipher = Cipher.getInstance(kon.STRING_ALGO_ENCRYPTOR)
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv)

                CipherOutputStream(output, cipher).use { cipherOut ->
                    val buffer = ByteArray(kon.STRING_READ_WRITE_BLOCK)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } > 0) {
                        cipherOut.write(buffer, 0, bytesRead)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                output.close()
            }
        }

        @Throws(
            NoSuchAlgorithmException::class,
            NoSuchPaddingException::class,
            InvalidAlgorithmParameterException::class,
            InvalidKeyException::class,
            IOException::class
        )
        fun decodeToFile(keyStr: String, input: InputStream, output: OutputStream) {
            try {
                val ivBytes = ByteArray(16)
                var totalRead = 0
                while (totalRead < 16) {
                    val read = input.read(ivBytes, totalRead, 16 - totalRead)
                    if (read == -1) throw IOException("Failed to read IV from stream")
                    totalRead += read
                }
                val iv = IvParameterSpec(ivBytes)
                val keySpec = SecretKeySpec(keyStr.toByteArray(StandardCharsets.UTF_8), kon.STRING_ALGO)

                val cipher = Cipher.getInstance(kon.STRING_ALGO_ENCRYPTOR)
                cipher.init(Cipher.DECRYPT_MODE, keySpec, iv)

                CipherOutputStream(output, cipher).use { cipherOut ->
                    val buffer = ByteArray(kon.STRING_READ_WRITE_BLOCK)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } > 0) {
                        cipherOut.write(buffer, 0, bytesRead)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                output.close()
            }
        }
    }
}
