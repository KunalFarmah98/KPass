import java.security.SecureRandom

object PasswordGenerator {
    fun generateSecurePassword(length: Int): String {
        // Define character pools.
        val lowerPool = "abcdefghijklmnopqrstuvwxyz"
        val upperPool = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val digitsPool = "0123456789"
        val specialPool = "!@#$%^&*()_-+=<>?/[]{}|~.,"

        // Maximum unique characters available:
        val maxUnique =
            26 + digitsPool.length + specialPool.length  // 26 letters (ignoring case) + 10 digits + 10 specials = 46
        if (length > maxUnique) {
            throw IllegalArgumentException("Requested length $length exceeds maximum unique characters: $maxUnique")
        }

        // A secure random generator.
        val secureRandom = SecureRandom()

        // Our candidate characters and a set for letters (to ensure case-insensitive uniqueness)
        val candidates = mutableListOf<Char>()
        val usedLetters = mutableSetOf<Char>() // stores letters in lowercase

        // Helper function to add letters from a given pool, using toList() for safe filtering.
        fun addLetter(fromPool: String, count: Int = 1) {
            val available =
                fromPool.toList().filter { ch -> !usedLetters.contains(ch.toLowerCase()) }
            if (available.size < count) {
                throw IllegalArgumentException("Not enough unique letters available from pool $fromPool")
            }
            available.shuffled(secureRandom).take(count).forEach { ch ->
                candidates.add(ch)
                usedLetters.add(ch.toLowerCase())
            }
        }

        // Helper function to add digits (they are simple; uniqueness is checked against current candidates)
        fun addDigit(count: Int) {
            val available = digitsPool.toList().filter { ch -> ch !in candidates }
            if (available.size < count) {
                throw IllegalArgumentException("Not enough unique digits available")
            }
            available.shuffled(secureRandom).take(count).forEach {
                candidates.add(it)
            }
        }

        // Helper function to add special characters.
        fun addSpecial(count: Int) {
            val available = specialPool.toList().filter { ch -> ch !in candidates }
            if (available.size < count) {
                throw IllegalArgumentException("Not enough unique special characters available")
            }
            available.shuffled(secureRandom).take(count).forEach {
                candidates.add(it)
            }
        }

        // Build the candidate list based on the required constraints.
        if (length > 8) {
            // Mandatory counts per constraints.
            addLetter(lowerPool, 3)    // 3 lowercase letters.
            addLetter(upperPool, 2)    // 2 uppercase letters.
            addDigit(2)                // 2 digits.
            addSpecial(2)              // 2 special characters.
            // Mandatory added: 3 + 2 + 2 + 2 = 9.
            val mandatoryCount = 9
            val extraCount = length - mandatoryCount

            // Add extra characters while ensuring uniqueness and orderability.
            for (i in 0 until extraCount) {
                val availableChars = mutableListOf<Char>()
                availableChars.addAll(
                    lowerPool.toList()
                        .filter { it !in candidates && !usedLetters.contains(it.toLowerCase()) })
                availableChars.addAll(
                    upperPool.toList()
                        .filter { it !in candidates && !usedLetters.contains(it.toLowerCase()) })
                availableChars.addAll(digitsPool.toList().filter { it !in candidates })
                availableChars.addAll(specialPool.toList().filter { it !in candidates })

                if (availableChars.isEmpty()) break

                // Validate before adding a special: ensure that the specials remain interspersible.
                val currentLetters = candidates.count { it.isLetter() }
                val currentDigits = candidates.count { it.isDigit() }
                val currentNonSpecial = currentLetters + currentDigits
                val currentSpecial = candidates.count { specialPool.contains(it) }

                val filtered = availableChars.filter { ch ->
                    if (specialPool.contains(ch)) {
                        (currentSpecial + 1) <= currentNonSpecial
                    } else {
                        true
                    }
                }

                val extraOptions = if (filtered.isNotEmpty()) filtered else availableChars
                val chosen = extraOptions.shuffled(secureRandom).first()
                if (chosen.isLetter()) {
                    usedLetters.add(chosen.toLowerCase())
                }
                candidates.add(chosen)
            }
        } else {
            // For passwords 8 characters or less, ensure that the first character will be a letter.
            addLetter(lowerPool, 1)
            for (i in 1 until length) {
                val availableChars = mutableListOf<Char>()
                availableChars.addAll(
                    lowerPool.toList()
                        .filter { it !in candidates && !usedLetters.contains(it.toLowerCase()) })
                availableChars.addAll(
                    upperPool.toList()
                        .filter { it !in candidates && !usedLetters.contains(it.toLowerCase()) })
                availableChars.addAll(digitsPool.toList().filter { it !in candidates })
                availableChars.addAll(specialPool.toList().filter { it !in candidates })
                if (availableChars.isEmpty()) break
                val chosen = availableChars.shuffled(secureRandom).first()
                if (chosen.isLetter()) {
                    usedLetters.add(chosen.toLowerCase())
                }
                candidates.add(chosen)
            }
        }

        // Arrange the candidates so that:
        // (a) The first character is a letter.
        // (b) No two special characters are adjacent.
        val maxShuffleAttempts = 1000
        var chosenPassword: String? = null
        val candidateSnapshot = candidates.toList()
        for (attempt in 0 until maxShuffleAttempts) {
            val shuffled = candidateSnapshot.shuffled(secureRandom)
            if (!shuffled.first().isLetter()) continue
            var valid = true
            for (i in 1 until shuffled.size) {
                if (specialPool.contains(shuffled[i]) && specialPool.contains(shuffled[i - 1])) {
                    valid = false
                    break
                }
            }
            if (valid) {
                chosenPassword = shuffled.joinToString("")
                break
            }
        }

        if (chosenPassword == null) {
            val result = CharArray(candidates.size)
            val usedIndex = BooleanArray(candidates.size)

            fun backtrack(pos: Int): Boolean {
                if (pos == result.size) return true
                for (i in candidates.indices.shuffled(secureRandom)) {
                    if (usedIndex[i]) continue
                    val ch = candidates[i]
                    if (pos == 0 && !ch.isLetter()) continue
                    if (pos > 0 && specialPool.contains(result[pos - 1]) && specialPool.contains(ch)) continue
                    usedIndex[i] = true
                    result[pos] = ch
                    if (backtrack(pos + 1)) return true
                    usedIndex[i] = false
                }
                return false
            }

            if (!backtrack(0)) {
                throw RuntimeException("Unable to generate valid password permutation")
            }
            chosenPassword = String(result)
        }
        return chosenPassword
    }
}