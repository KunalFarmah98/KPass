package com.apps.kunalfarmah.kpass.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordGeneratorTest {

    @Test
    fun `Valid password length`() {
        val passwordLength = 12
        val password = PasswordGenerator.generateSecurePassword(passwordLength)
        assertEquals(passwordLength, password.length)
    }

    @Test
    fun `Minimum password length`() {
        val password = PasswordGenerator.generateSecurePassword(5)
        assertEquals(5, password.length)
    }

    @Test
    fun `Password length 8`() {
        val password = PasswordGenerator.generateSecurePassword(8)
        assertEquals(8, password.length)
        assertTrue(password.matches(Regex(".*[A-Z].*")))
        assertTrue(password.matches((Regex(".*[a-z].*"))))
        assertTrue(password.matches((Regex(".*[0-9].*"))))
    }

    @Test
    fun `Password length 10`() {
        val password = PasswordGenerator.generateSecurePassword(10)
        assertEquals(10, password.length)
        assertTrue(password.matches(Regex(".*[A-Z].*")))
        assertTrue(password.matches((Regex(".*[a-z].*"))))
        assertTrue(password.matches((Regex(".*[0-9].*"))))
    }

    @Test
    fun `Password length 15`() {
        val password = PasswordGenerator.generateSecurePassword(15)
        assertEquals(15, password.length)
        assertTrue(password.matches(Regex(".*[A-Z].*")))
        assertTrue(password.matches((Regex(".*[a-z].*"))))
        assertTrue(password.matches((Regex(".*[0-9].*"))))
    }

    @Test
    fun `Password length 20`() {
        val password = PasswordGenerator.generateSecurePassword(20)
        assertEquals(20, password.length)
        assertTrue(password.matches(Regex(".*[A-Z].*")))
        assertTrue(password.matches((Regex(".*[a-z].*"))))
        assertTrue(password.matches((Regex(".*[0-9].*"))))
    }

    @Test
    fun `Password length 25`() {
        val password = PasswordGenerator.generateSecurePassword(25)
        assertEquals(25, password.length)
        assertTrue(password.matches(Regex(".*[A-Z].*")))
        assertTrue(password.matches((Regex(".*[a-z].*"))))
        assertTrue(password.matches((Regex(".*[0-9].*"))))
    }

    @Test
    fun `Password length 30`() {
        val password = PasswordGenerator.generateSecurePassword(30)
        assertEquals(30, password.length)
        assertTrue(password.matches(Regex(".*[A-Z].*")))
        assertTrue(password.matches((Regex(".*[a-z].*"))))
        assertTrue(password.matches((Regex(".*[0-9].*"))))
    }

    @Test
    fun `Password contains uppercase`() {
        val password = PasswordGenerator.generateSecurePassword(15)
        assertTrue(password.matches(Regex(".*[A-Z].*")))
    }

    @Test
    fun `Password contains lowercase`() {
        val password = PasswordGenerator.generateSecurePassword(15)
        assertTrue(password.matches(Regex(".*[a-z].*")))
    }

    @Test
    fun `Password contains digits`() {
        val password = PasswordGenerator.generateSecurePassword(15)
        assertTrue(password.matches(Regex(".*[0-9].*")))
    }

    @Test
    fun `Password content shuffled`() {
        val password1 = PasswordGenerator.generateSecurePassword(20)
        val password2 = PasswordGenerator.generateSecurePassword(20)
        assertNotEquals(password1, password2)
    }

    @Test
    fun `Multiple password generations`() {
        val length = 20
        val numPasswords = 1000000 // Generate 10,00,000 passwords
        val generatedPasswords = HashSet<String>() // Use a Set to track unique passwords
        var duplicateFound = false

        for (i in 0 until numPasswords) {
            val newPassword = PasswordGenerator.generateSecurePassword(length)
            // Check if the new password is already in the set
            if (!generatedPasswords.add(newPassword)) {
                duplicateFound = true
                break // Exit the loop early since we found a duplicate
            }
        }
        // Assert that no duplicates were found
        assertTrue(!duplicateFound)
    }

    @Test
    fun `Password always starts with alphabet`(){
        val numPasswords = 100
        for(i in 0 until numPasswords){
            val password = PasswordGenerator.generateSecurePassword(15)
            assertTrue(password.matches(Regex("^[A-Za-z].*")))
        }
    }

}