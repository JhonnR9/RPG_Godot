package game.repository

import godot.global.GD
import java.io.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.crypto.CipherOutputStream
import javax.crypto.CipherInputStream
import io.github.cdimascio.dotenv.dotenv


class Repository {
    private val dotenv = dotenv()
    private val folderName = "RPGGame"
    private val fileName = "SaveGame.rpg"
    private val secretKey = dotenv["GAME_SECRET_KEY"] ?: "defaultSecretKey1234"

    private fun getSaveFileName(): String {
        val saveDir = File(System.getProperty("user.home"), folderName)
        if (!saveDir.exists()) saveDir.mkdirs()
        return File(saveDir, fileName).toString()
    }

    private fun getCipher(mode: Int): Cipher {
        val key: SecretKey = SecretKeySpec(secretKey.toByteArray(), "AES")
        return Cipher.getInstance("AES").apply { init(mode, key) }
    }

    fun save(data: Serializable): Boolean {
        return try {
            val cipher = getCipher(Cipher.ENCRYPT_MODE)
            CipherOutputStream(FileOutputStream(getSaveFileName()), cipher).use { output ->
                ObjectOutputStream(output).use { it.writeObject(data) }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getData(): Any? {
        val saveFile = File(getSaveFileName())
        if (!saveFile.exists()) {
            return null
        }

        return try {
            val cipher = getCipher(Cipher.DECRYPT_MODE)
            CipherInputStream(FileInputStream(saveFile), cipher).use { input ->
                ObjectInputStream(input).use { it.readObject() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
