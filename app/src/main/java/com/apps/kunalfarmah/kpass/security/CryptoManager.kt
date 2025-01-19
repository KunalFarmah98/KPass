package com.apps.kunalfarmah.kpass.security

import android.R.attr.password
import android.util.Base64
import com.apps.kunalfarmah.kpass.constant.Constants
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random


object CryptoManager {

    private val keyAndIv = getEncryptionKeyAndIv()

    private fun encryptAES(plainText: String, key: String, iv: ByteArray): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKeySpec = SecretKeySpec(key.toByteArray(), "AES")
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
        val encryptedBytes = cipher.doFinal(plainText.toByteArray())
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    private fun decryptAES(encryptedText: String, key: String, iv: ByteArray): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKeySpec = SecretKeySpec(key.toByteArray(), "AES")
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
        val encryptedBytes = Base64.decode(encryptedText, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes)
    }

    fun encryptData(data: String): String{
        return encryptAES(data, keyAndIv.first, keyAndIv.second)
    }

    fun decryptData(data: String): String{
        return decryptAES(data, keyAndIv.first, keyAndIv.second)
    }

    private fun getEncryptionKeyAndIv() : Pair<String,ByteArray>{

        val keygen = KeyGenerator.getInstance("AES")
        keygen.init(256)
        val key: SecretKey = keygen.generateKey()
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val ciphertext: ByteArray = cipher.doFinal(Constants.masterPass.toByteArray())
        val iv: ByteArray = cipher.iv
        return Pair(Base64.encodeToString(ciphertext, Base64.DEFAULT),iv)
    }
}