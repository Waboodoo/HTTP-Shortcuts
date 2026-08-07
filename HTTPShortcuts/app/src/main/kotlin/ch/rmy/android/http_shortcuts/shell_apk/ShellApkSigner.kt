package ch.rmy.android.http_shortcuts.shell_apk

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.android.apksig.ApkSigner
import java.io.File
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.Date
import javax.inject.Inject
import javax.security.auth.x500.X500Principal

/**
 * Signs generated shell APKs with one app-local key.
 *
 * Android requires every APK to be signed, including APKs generated locally on the device. The key is created once in
 * Android Keystore and then reused so reinstalling a shell APK with the same package name works as an update.
 */
class ShellApkSigner
@Inject
constructor() {

    fun sign(inputApk: File, outputApk: File) {
        val signerConfig = getSignerConfig()
        ApkSigner.Builder(listOf(signerConfig))
            .setInputApk(inputApk)
            .setOutputApk(outputApk)
            .setMinSdkVersion(MIN_SDK_VERSION)
            .setV1SigningEnabled(true)
            .setV2SigningEnabled(true)
            .setV3SigningEnabled(true)
            .build()
            .sign()
    }

    private fun getSignerConfig(): ApkSigner.SignerConfig {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            // The private key is intentionally non-exportable and scoped to this app install.
            generateKey()
            keyStore.load(null)
        }
        val privateKey = keyStore.getKey(KEY_ALIAS, null) as PrivateKey
        val certificate = keyStore.getCertificate(KEY_ALIAS) as X509Certificate
        return ApkSigner.SignerConfig.Builder(KEY_ALIAS, privateKey, listOf(certificate))
            .build()
    }

    private fun generateKey() {
        val generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE)
        generator.initialize(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY,
            )
                .setCertificateSubject(X500Principal("CN=HTTP Shortcuts Shell APK"))
                .setCertificateSerialNumber(BigInteger.ONE)
                .setCertificateNotBefore(Date(0))
                .setCertificateNotAfter(Date(System.currentTimeMillis() + CERTIFICATE_VALIDITY_MS))
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                .build(),
        )
        generator.generateKeyPair()
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "http_shortcuts_shell_apk"
        private const val MIN_SDK_VERSION = 26
        private const val CERTIFICATE_VALIDITY_MS = 30 * 365 * 24 * 60 * 60 * 1000L
    }
}
