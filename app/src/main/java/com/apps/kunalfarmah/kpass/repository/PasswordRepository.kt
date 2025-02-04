package com.apps.kunalfarmah.kpass.repository

import com.apps.kunalfarmah.kpass.db.PasswordMap
import com.apps.kunalfarmah.kpass.db.PasswordMapDao
import com.apps.kunalfarmah.kpass.security.CryptoManager

class PasswordRepository(private val passwordMapDao: PasswordMapDao){

    suspend fun insertOrUpdatePassword(websiteName: String, websiteUrl: String?, username: String, password: String){
        val encryptedPassword = CryptoManager.encrypt(password)
        passwordMapDao.insertPassword(PasswordMap(websiteName, websiteUrl, username, encryptedPassword))
    }

    suspend fun getAllPasswords(): List<PasswordMap>{
        return passwordMapDao.getAllPasswords()
    }

    suspend fun deletePassword(username: String){
        passwordMapDao.deletePassword(username)
    }

}