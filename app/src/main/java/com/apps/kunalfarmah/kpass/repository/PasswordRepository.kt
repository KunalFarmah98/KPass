package com.apps.kunalfarmah.kpass.repository

import com.apps.kunalfarmah.kpass.db.PasswordMap
import com.apps.kunalfarmah.kpass.db.PasswordMapDao
import com.apps.kunalfarmah.kpass.security.CryptoManager

class PasswordRepository(private val passwordMapDao: PasswordMapDao){

    suspend fun insertOrUpdatePassword(id: String = "", websiteName: String, websiteUrl: String?, username: String, password: String
                                       , lastModified: Long? = System.currentTimeMillis(), isIgnored: Int = 0, isUpdate: Boolean = false){
        val encryptedPassword = CryptoManager.encrypt(password)
        // do not update lastModified if only isIgnored is updated
        if (isUpdate) {
            val existingEntry = passwordMapDao.getPasswordById(id)
            // Decrypt existing password for comparison
            val existingRawPassword = try {
                CryptoManager.decrypt(existingEntry.password)
            } catch (_: Exception) {}
            // Determine if only 'isIgnored' has changed
            val onlyIgnoredChanged =
                existingEntry.websiteName == websiteName &&
                        existingEntry.websiteUrl == websiteUrl &&
                        existingEntry.username == username &&
                        existingRawPassword == password && // Compare with incoming RAW password
                        existingEntry.isIgnored != isIgnored // isIgnored must have changed

            val finalLastModified = if (onlyIgnoredChanged) {
                // Only isIgnored changed, and no other field changed
                existingEntry.lastModified
            } else {
                // Any other field changed, OR isIgnored changed ALONG WITH other fields,
                // OR it's a new field being set (e.g. from null to a value for websiteUrl)
                lastModified!!
            }
            val passwordMap = PasswordMap(
                id = id,
                websiteName = websiteName,
                websiteUrl = websiteUrl,
                username = username,
                password = encryptedPassword,
                lastModified = finalLastModified,
                isIgnored = isIgnored
            )
            passwordMapDao.updatePassword(passwordMap)
        }
        else {
            val passwordMap = PasswordMap(
                id = id,
                websiteName = websiteName,
                websiteUrl = websiteUrl,
                username = username,
                password = encryptedPassword,
                lastModified = lastModified!!,
                isIgnored = isIgnored
            )
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