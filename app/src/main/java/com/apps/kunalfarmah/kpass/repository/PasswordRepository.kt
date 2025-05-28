package com.apps.kunalfarmah.kpass.repository

import com.apps.kunalfarmah.kpass.db.PasswordMap
import com.apps.kunalfarmah.kpass.db.PasswordMapDao
import com.apps.kunalfarmah.kpass.security.CryptoManager

class PasswordRepository(private val passwordMapDao: PasswordMapDao){

    suspend fun insertOrUpdatePassword(id: String = "", websiteName: String, websiteUrl: String?, username: String, password: String
                                       , lastModified: Long? = System.currentTimeMillis(), isIgnored: Int = 0, isUpdate: Boolean = false){
        val encryptedPassword = CryptoManager.encrypt(password)
        val passwordMap = PasswordMap(
            id = id,
            websiteName = websiteName,
            websiteUrl = websiteUrl,
            username = username,
            password = encryptedPassword,
            lastModified = lastModified!!,
            isIgnored = isIgnored
        )
        if(isUpdate){
            passwordMapDao.updatePassword(passwordMap)
        }
        else{
            passwordMapDao.insertPassword(passwordMap)
        }
    }

    suspend fun getAllPasswords(): List<PasswordMap>{
        return passwordMapDao.getAllPasswords()
    }

    suspend fun getAllOldPasswords(): List<PasswordMap>{
        return passwordMapDao.getAllOldPasswords()
    }

    suspend fun deletePassword(id: String){
        passwordMapDao.deletePassword(id)
    }

    suspend fun deleteAllPasswords(){
        passwordMapDao.deleteAllPasswords()
    }

}