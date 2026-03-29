package com.apps.kunalfarmah.kpass.model

/**
 * Data Transfer Object for passwords being imported from a PDF.
 * This class holds the plaintext password temporarily before it is encrypted and stored in the database.
 */
data class ImportedPassword(
    val websiteName: String,
    val websiteUrl: String?,
    val username: String,
    val rawPassword: String
)
