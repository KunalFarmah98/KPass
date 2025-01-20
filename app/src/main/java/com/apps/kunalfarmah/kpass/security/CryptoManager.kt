package com.apps.kunalfarmah.kpass.security
import android.annotation.SuppressLint
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.byteArrayPreferencesKey
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
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@SuppressLint("StaticFieldLeak")
object CryptoManager {

    val keystore: KeyStore = KeyStore.getInstance("AndroidKeyStore")
    lateinit var context: Context
    private var secretKey : SecretKey? = null
    private var privateKey: ByteArray? = null

    init{
        keystore.load(null)
//        keystore.deleteEntry(Constants.KEY_MASTER)
        // get secretKey from keystore
        secretKey = keystore.getKey(Constants.KEY_MASTER,null) as? SecretKey
        // if secret key exists, get private key from dataStore and decrypt it
        secretKey?.let {key ->
            CoroutineScope(Dispatchers.IO).launch {
                context.dataStore.data.collect{
                    val encryptedPrivateKey = it[stringPreferencesKey(Constants.KEY_PRIVATE)]
                    if(encryptedPrivateKey != null){
                        Log.d("CryptoManager", "received privateKey: $encryptedPrivateKey")
                        privateKey = decryptAES(encryptedPrivateKey, key).toByteArray()
                    }
                    Log.d("CryptoManager", "secretKey: $secretKey")
                    Log.d("CryptoManager", "privateKey: ${privateKey.toString()}")
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

    private fun encryptAES(plainText: String, key: SecretKey): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plainText.toByteArray())
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT) + ":" + Base64.encodeToString(iv, Base64.DEFAULT)
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

    private fun decryptAES(encryptedText: String, key: SecretKey): String {
        val parts = encryptedText.split(":")
        val encryptedData = Base64.decode(parts[0], Base64.DEFAULT)
        val iv = Base64.decode(parts[1], Base64.DEFAULT)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmParameterSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec)
        val decryptedBytes = cipher.doFinal(encryptedData)
        return String(decryptedBytes)
    }

    fun encryptData(data: String): String{
        return encryptAES(data, privateKey!!)
    }

    fun decryptData(data: String): String{
        return decryptAES(data, privateKey!!)
    }

    fun generateSecretKeyFromPassword(password: String, iterations: Int = 65536, keyLength: Int = 256) {
        // if key is already stored, get its value or create and store a new salt
        val salt = ByteArray(32)
        SecureRandom().nextBytes(salt)
        val factory: SecretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt, iterations, keyLength)
        // generate the private key
        val privKey = factory.generateSecret(spec)

        val keyGenerator =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                Constants.KEY_MASTER,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        // this key will encrypt the private key used for encryption
        secretKey = keyGenerator.generateKey()

        // set privateKey
        privateKey = privKey.encoded

        val encryptedPrivateKey = encryptAES(privateKey!!.toString(), secretKey!!)
        Log.d("CryptoManager", "encrypted privateKey: $encryptedPrivateKey")

        // store encrypted privateKey in datastore
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.updateData {
                it.toMutablePreferences().apply {
                    this[stringPreferencesKey(Constants.KEY_PRIVATE)] = encryptedPrivateKey
                }
            }
        }

        Log.d("CryptoManager", "new secretKey: $secretKey")
        Log.d("CryptoManager", "new privateKey: ${privateKey.toString()}")

    }
}