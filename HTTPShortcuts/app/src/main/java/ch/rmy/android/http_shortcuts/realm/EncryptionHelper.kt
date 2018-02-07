package ch.rmy.android.http_shortcuts.realm

import android.annotation.TargetApi
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.support.annotation.RequiresApi
import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.security.InvalidAlgorithmParameterException
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.SecureRandom
import java.security.UnrecoverableEntryException
import java.security.interfaces.RSAPublicKey
import java.util.*
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.security.auth.x500.X500Principal

class EncryptionHelper(private val context: Context) {

    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore")
    private val preferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    init {
        keyStore.load(null)
    }

    val encryptionKey: ByteArray
        get() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                return FALLBACK_KEY
            }
            return getOrGenerateKey()
        }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun getOrGenerateKey(): ByteArray {
        if (!preferences.contains(PREF_KEY)) {
            generateAndStoreKey()
        }
        val encryptedKey = Base64.decode(preferences.getString(PREF_KEY, ""), Base64.DEFAULT)
        return decrypt(encryptedKey)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Throws(KeyStoreException::class, UnrecoverableEntryException::class, NoSuchAlgorithmException::class, NoSuchProviderException::class, InvalidAlgorithmParameterException::class)
    private fun getPrivateKeyEntry(): KeyStore.PrivateKeyEntry {
        if (!keyStore.containsAlias(ALIAS)) {
            generateKeyPair(context)
        }
        return keyStore.getEntry(ALIAS, null) as KeyStore.PrivateKeyEntry
    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun generateAndStoreKey() {
        val encryptionKey = generateKey()
        val encryptedEncryptionKey = encrypt(encryptionKey)
        val encodedEncryptedEncryptionKey = Base64.encodeToString(encryptedEncryptionKey, Base64.DEFAULT)
        preferences.edit().putString(PREF_KEY, encodedEncryptedEncryptionKey).apply()
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun encrypt(plainText: ByteArray): ByteArray {
        val privateKeyEntry = getPrivateKeyEntry()
        val publicKey = privateKeyEntry.certificate.publicKey as RSAPublicKey

        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)

        ByteArrayOutputStream().use { outputStream ->
            CipherOutputStream(outputStream, cipher).use {
                it.write(plainText)
            }
            return outputStream.toByteArray()
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun decrypt(cipherText: ByteArray): ByteArray {
        val privateKeyEntry = getPrivateKeyEntry()

        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKeyEntry.privateKey)

        CipherInputStream(ByteArrayInputStream(cipherText), cipher).use { inputStream ->
            ByteArrayOutputStream().use { outputStream ->
                val buffer = ByteArray(1024)
                while (true) {
                    val length = inputStream.read(buffer)
                    if (length == -1) {
                        break
                    }
                    outputStream.write(buffer, 0, length)
                }
                return outputStream.toByteArray()
            }
        }
    }

    private fun generateKey() = ByteArray(KEY_LENGTH).also { key ->
        SecureRandom().nextBytes(key)
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun generateKeyPair(context: Context) {
        val start = Calendar.getInstance()
        val end = Calendar.getInstance()
        end.add(Calendar.YEAR, 30)
        val spec = KeyPairGeneratorSpec.Builder(context)
                .setAlias(ALIAS)
                .setSubject(X500Principal("CN=Database Key, O=rmy.ch"))
                .setSerialNumber(BigInteger.ONE)
                .setStartDate(start.time)
                .setEndDate(end.time)
                .build()

        val generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore")
        generator.initialize(spec)
        generator.generateKeyPair()
    }

    companion object {

        private val FALLBACK_KEY = "ZX06poC7a96dL9,FR_9|Ww<2%]?4Ij(3wR3DmyNj0[{(,8g%jX2{03P45_p`N6|2".toByteArray()

        private const val KEY_LENGTH = 64
        private const val ALIAS = "db_encryption_keypair"

        private const val PREF_NAME = "realm.encryption"
        private const val PREF_KEY = "encrypted_key"

    }

}