package com.apps.kunalfarmah.kpass.security
import android.annotation.SuppressLint
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.apps.kunalfarmah.kpass.constant.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.KeyStore
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@SuppressLint("StaticFieldLeak")
object CryptoManager {

    lateinit var context: Context
    private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    private const val AES_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
    private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
    private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
    private const val TRANSFORMATION = "$AES_ALGORITHM/$BLOCK_MODE/$PADDING"
    private lateinit var privateKey: ByteArray

    private val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply {
        load(null) // With load function we initialize our keystore
//        deleteEntry(Constants.KEY_MASTER)
    }

    init{
        CoroutineScope(Dispatchers.IO).launch {
            val secretKey = getOrCreateKey()
            context.dataStore.data.collect{
                val encryptedPrivateKey = it[stringPreferencesKey(Constants.KEY_PRIVATE)]
                if(encryptedPrivateKey != null){
                    Log.d("CryptoManager", "received privateKey: $encryptedPrivateKey")
                    privateKey = decryptAES(encryptedPrivateKey).toByteArray()
                    Log.d("CryptoManager", "secretKey: $secretKey")
                    Log.d("CryptoManager", "privateKey: $privateKey")
                }
            }
        }
    }


    private fun encryptAES(plainText: String, key: ByteArray): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey: SecretKey = SecretKeySpec(key, "AES")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plainText.toByteArray())
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT) + ":" + Base64.encodeToString(iv, Base64.DEFAULT)
    }

    private fun encryptAES(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())

        val encryptedBytes = cipher.doFinal(plainText.toByteArray())
        val iv = cipher.iv

        val encryptedDataWithIV = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, encryptedDataWithIV, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, encryptedDataWithIV, iv.size, encryptedBytes.size)
        return Base64.encodeToString(encryptedDataWithIV, Base64.DEFAULT)
    }

    private fun decryptAES(encryptedText: String, key: ByteArray): String {
        val parts = encryptedText.split(":")
        val encryptedData = Base64.decode(parts[0], Base64.DEFAULT)
        val iv = Base64.decode(parts[1], Base64.DEFAULT)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey: SecretKey = SecretKeySpec(key, "AES")
        val gcmParameterSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec)
        val decryptedBytes = cipher.doFinal(encryptedData)
        return String(decryptedBytes)
    }

    private fun decryptAES(encryptedText: String): String {
        val encryptedDataWithIV = Base64.decode(encryptedText, Base64.DEFAULT)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = encryptedDataWithIV.copyOfRange(0, cipher.blockSize)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), IvParameterSpec(iv))

        val encryptedData = encryptedDataWithIV.copyOfRange(cipher.blockSize, encryptedDataWithIV.size)
        val decryptedBytes = cipher.doFinal(encryptedData)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    private fun createKey(): SecretKey {
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            Constants.KEY_MASTER,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(BLOCK_MODE)
            .setEncryptionPaddings(PADDING)
            .setUserAuthenticationRequired(false)
            .setRandomizedEncryptionRequired(true)
            .build()

        return KeyGenerator.getInstance(AES_ALGORITHM).apply {
            init(keyGenParameterSpec)
        }.generateKey()
    }

    private fun getOrCreateKey(): SecretKey {
        val existingKey = keyStore.getEntry(Constants.KEY_MASTER, null) as? KeyStore.SecretKeyEntry
        val key = existingKey?.secretKey ?: createKey()
        Log.d("CryptoManager", "existingKey: $key")
        return key
    }

    fun encryptData(data: String): String{
        return encryptAES(data)
    }

    fun decryptData(data: String): String{
        return decryptAES(data)
    }

    fun generateSecretKeyFromPassword(password: String, iterations: Int = 65536, keyLength: Int = 256) {
        // if key is already stored, get its value or create and store a new salt
        val salt = ByteArray(32)
        SecureRandom().nextBytes(salt)
        val factory: SecretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt, iterations, keyLength)
        // generate the private key
        val privKey = factory.generateSecret(spec)

        // set privateKey
        privateKey = privKey.encoded

        Log.d("CryptoManager", "new privateKey: ${privateKey.toString()}")

        val encryptedPrivateKey = encryptAES(privateKey.toString())
        Log.d("CryptoManager", "encrypted privateKey: $encryptedPrivateKey")

        // store encrypted privateKey in datastore
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.updateData {
                it.toMutablePreferences().apply {
                    this[stringPreferencesKey(Constants.KEY_PRIVATE)] = encryptedPrivateKey
                }
            }
        }



    }
}