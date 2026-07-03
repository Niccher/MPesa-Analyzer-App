package com.niccher.mpesa_analyzer_app.helpers

import com.niccher.mpesa_analyzer_app.konstants.Konstants
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
        private val kon = Konstants

        @Throws(
            NoSuchAlgorithmException::class,
            NoSuchPaddingException::class,
            InvalidAlgorithmParameterException::class,
            InvalidKeyException::class,
            IOException::class
        )
        fun encodeToFile(keyStr: String, speStr: String, input: InputStream, output: OutputStream) {
            try {
                val iv = IvParameterSpec(speStr.toByteArray(StandardCharsets.UTF_8))
                val keySpec = SecretKeySpec(keyStr.toByteArray(StandardCharsets.UTF_8), kon.STRING_ALGO)

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
        fun decodeToFile(keyStr: String, speStr: String, input: InputStream, output: OutputStream) {
            try {
                val iv = IvParameterSpec(speStr.toByteArray(StandardCharsets.UTF_8))
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
