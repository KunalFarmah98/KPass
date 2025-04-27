package com.apps.kunalfarmah.kpass.utils

import org.junit.Assert
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
        println("password: $password")
        assertEquals(8, password.length)
        assertTrue(password.matches(Regex(".*[A-Z].*")))
        assertTrue(password.matches((Regex(".*[a-z].*"))))
        assertTrue(password.matches((Regex(".*[0-9].*"))))
        assertTrue(password.matches((Regex(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~].*"))))
    }

    @Test
    fun `Password length 10`() {
        val password = PasswordGenerator.generateSecurePassword(10)
        println("password: $password")
        assertEquals(10, password.length)
        assertTrue(password.matches(Regex(".*[A-Z].*")))
        assertTrue(password.matches((Regex(".*[a-z].*"))))
        assertTrue(password.matches((Regex(".*[0-9].*"))))
        assertTrue(password.matches((Regex(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~].*"))))
    }

    @Test
    fun `Password length 15`() {
        val password = PasswordGenerator.generateSecurePassword(15)
        println("password: $password")
        assertEquals(15, password.length)
        assertTrue(password.matches(Regex(".*[A-Z].*")))
        assertTrue(password.matches((Regex(".*[a-z].*"))))
        assertTrue(password.matches((Regex(".*[0-9].*"))))
        assertTrue(password.matches((Regex(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~].*"))))
    }

    @Test
    fun `Password length 20`() {
        val password = PasswordGenerator.generateSecurePassword(20)
        println("password: $password")
        assertEquals(20, password.length)
        assertTrue(password.matches(Regex(".*[A-Z].*")))
        assertTrue(password.matches((Regex(".*[a-z].*"))))
        assertTrue(password.matches((Regex(".*[0-9].*"))))
        assertTrue(password.matches((Regex(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~].*"))))
    }

    @Test
    fun `Password length 25`() {
        val password = PasswordGenerator.generateSecurePassword(25)
        println("password: $password")
        assertEquals(25, password.length)
        assertTrue(password.matches(Regex(".*[A-Z].*")))
        assertTrue(password.matches((Regex(".*[a-z].*"))))
        assertTrue(password.matches((Regex(".*[0-9].*"))))
        assertTrue(password.matches((Regex(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~].*"))))
    }

    @Test
    fun `Password length 30`() {
        val password = PasswordGenerator.generateSecurePassword(30)
        assertEquals(30, password.length)
        assertTrue(password.matches(Regex(".*[A-Z].*")))
        assertTrue(password.matches((Regex(".*[a-z].*"))))
        assertTrue(password.matches((Regex(".*[0-9].*"))))
        assertTrue(password.matches((Regex(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~].*"))))
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
    fun `Password contains special characters`() {
        val password = PasswordGenerator.generateSecurePassword(15)
        assertTrue(password.matches(Regex(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~.].*")))
    }

    @Test
    fun `Password content shuffled`() {
        val password1 = PasswordGenerator.generateSecurePassword(20)
        val password2 = PasswordGenerator.generateSecurePassword(20)
        assertNotEquals(password1, password2)
    }

    @Test
    fun `Password character set uniqueness`() {
        val password = PasswordGenerator.generateSecurePassword(15)
        // Check if the password contains characters outside the defined sets
        assertTrue(password.matches(Regex("^[A-Za-z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~.]+\$")))
    }

    @Test
    fun `Multiple password generations`() {
        val length = 12
        val passwords = mutableSetOf<String>()
        for (i in 0 until 10) {
            passwords.add(PasswordGenerator.generateSecurePassword(length))
        }
        assertEquals(10, passwords.size) // All passwords should be unique
    }

    @Test
    fun `Multiple passwords repetition and constraints test`() {
        val numPasswords = 100
        val passwordLength = 20
        val SPECIAL_CHARS = "!@#$%^&*()_-+=<>?/[]{}|~.,"

        for (i in 0 until numPasswords) {
            val password = PasswordGenerator.generateSecurePassword(passwordLength)
            println("password [$i]: $password")
            if (password.isEmpty()) continue

            var lowercaseCount = 0
            var uppercaseCount = 0
            var specialCharCount = 0
            var digitCount = 0
            var consecutiveSpecial = false
            val usedChars = mutableSetOf<Char>() // For unique character check

            for (j in password.indices) {
                val char = password[j]

                if (char.isDigit()) {
                    digitCount++
                } else if (char.isUpperCase()) {
                    uppercaseCount++
                } else if (char.isLowerCase()) {
                    lowercaseCount++
                } else if (char in SPECIAL_CHARS) {
                    specialCharCount++
                    if (j > 0 && password[j - 1] in SPECIAL_CHARS) {
                        consecutiveSpecial = true
                    }
                }

                // Check for unique characters (case-insensitive)
                Assert.assertFalse(
                    "Password must have unique characters (case-insensitive)",
                    usedChars.contains(char.lowercaseChar())
                )
                usedChars.add(char.lowercaseChar())
            }

            // Check length > 8 constraints
            if (passwordLength > 8) {
                assertTrue("Password must have at least 3 lowercase letters", lowercaseCount >= 3)
                assertTrue("Password must have at least 2 uppercase letters", uppercaseCount >= 2)
                assertTrue("Password must have at least 2 digits", digitCount >= 2)
                assertTrue(
                    "Password must have at least 2 special characters",
                    specialCharCount >= 2
                )
            }

            Assert.assertFalse("There should be no consecutive special chars", consecutiveSpecial)
            assertTrue(password.matches(Regex("^[A-Za-z].*"))) // Starts with an alphabet
        }
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