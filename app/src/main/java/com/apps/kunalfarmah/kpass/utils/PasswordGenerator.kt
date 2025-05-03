import com.apps.kunalfarmah.kpass.BuildConfig
import java.security.SecureRandom

object PasswordGenerator {

    val availablePasswordLengths = listOf<Int>(8, 15, 20, 25, 30)

    fun generateSecurePassword(length: Int): String {
        val lowerPool = BuildConfig.LOWERCASE_POOL
        val upperPool = BuildConfig.UPPERCASE_POOL
        val digitsPool = BuildConfig.DIGITS_POOL
        val specialPool = BuildConfig.SPECIAL_POOL

        // Maximum unique characters available:
        val maxUnique = 26 + digitsPool.length + specialPool.length  // 26 letters (ignoring case) + 10 digits + 10 specials = 46
        if (length > maxUnique) {
            throw IllegalArgumentException("Requested length $length exceeds maximum unique characters: $maxUnique")
        }

        val secureRandom = SecureRandom()
        val candidates = mutableListOf<Char>()
        // To guarantee uniqueness for letters irrespective of case.
        val usedLetters = mutableSetOf<Char>()

        // Helper: add letters from a pool while ensuring that neither the letter nor its opposite case has been used.
        fun addLetter(fromPool: String, count: Int = 1) {
            val available = fromPool.toList().filter { ch -> !usedLetters.contains(ch.lowercaseChar()) }
            if (available.size < count) {
                throw IllegalArgumentException("Not enough unique letters available from pool $fromPool")
            }
            available.shuffled(secureRandom).take(count).forEach { ch ->
                candidates.add(ch)
                usedLetters.add(ch.lowercaseChar())
            }
        }

        // Helper: add digits.
        fun addDigit(count: Int) {
            val available = digitsPool.toList().filter { ch -> ch !in candidates }
            if (available.size < count) {
                throw IllegalArgumentException("Not enough unique digits available")
            }
            available.shuffled(secureRandom).take(count).forEach {
                candidates.add(it)
            }
        }

        // Helper: add special characters.
        fun addSpecial(count: Int) {
            val available = specialPool.toList().filter { ch -> ch !in candidates }
            if (available.size < count) {
                throw IllegalArgumentException("Not enough unique special characters available")
            }
            available.shuffled(secureRandom).take(count).forEach {
                candidates.add(it)
            }
        }

        // Gather candidates according to the required constraints.
        if (length > 8) {
            // Add the mandatory counts.
            addLetter(lowerPool, BuildConfig.MIN_LOWER)
            addLetter(upperPool, BuildConfig.MIN_UPPER)
            addDigit(BuildConfig.MIN_DIGITS)
            addSpecial(BuildConfig.MIN_SPECIAL)
            val mandatoryCount = BuildConfig.MANDATORY_CHARS
            val extraCount = length - mandatoryCount

            // Add extra characters one by one from any pool while ensuring uniqueness.
            for (i in 0 until extraCount) {
                val availableChars = mutableListOf<Char>()
                availableChars.addAll(lowerPool.toList().filter { it !in candidates && !usedLetters.contains(it.lowercaseChar()) })
                availableChars.addAll(upperPool.toList().filter { it !in candidates && !usedLetters.contains(it.lowercaseChar()) })
                availableChars.addAll(digitsPool.toList().filter { it !in candidates })
                availableChars.addAll(specialPool.toList().filter { it !in candidates })

                if (availableChars.isEmpty()) break

                // For a special candidate, ensure that it won’t exceed the number of non-specials—
                // a necessary condition for interleaving.
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
                    usedLetters.add(chosen.lowercaseChar())
                }
                candidates.add(chosen)
            }
        } else {
            // For password lengths 8 or less, force one letter at the beginning.
            addLetter(lowerPool, 1)
            for (i in 1 until length) {
                val availableChars = mutableListOf<Char>()
                availableChars.addAll(lowerPool.toList().filter { it !in candidates && !usedLetters.contains(it.lowercaseChar()) })
                availableChars.addAll(upperPool.toList().filter { it !in candidates && !usedLetters.contains(it.lowercaseChar()) })
                availableChars.addAll(digitsPool.toList().filter { it !in candidates })
                availableChars.addAll(specialPool.toList().filter { it !in candidates })
                if (availableChars.isEmpty()) break
                val chosen = availableChars.shuffled(secureRandom).first()
                if (chosen.isLetter()) {
                    usedLetters.add(chosen.lowercaseChar())
                }
                candidates.add(chosen)
            }
        }

        // --- Arrangement Step ---
        // Rather than repeatedly trying random shuffles, partition the candidates into non‑specials (letters and digits)
        // and specials. Then, interleave the specials deterministically into the non‑special sequence.
        val nonSpecialCandidates = candidates.filter { !specialPool.contains(it) }.toMutableList()
        val specialCandidates = candidates.filter { specialPool.contains(it) }.toMutableList()

        // Randomize each group.
        nonSpecialCandidates.shuffle(secureRandom)
        specialCandidates.shuffle(secureRandom)

        // Ensure the first candidate is a letter.
        if (nonSpecialCandidates.isEmpty() || nonSpecialCandidates.none { it.isLetter() }) {
            throw RuntimeException("No letter available to start the password")
        }
        if (!nonSpecialCandidates[0].isLetter()) {
            val letterIndex = nonSpecialCandidates.indexOfFirst { it.isLetter() }
            val temp = nonSpecialCandidates[0]
            nonSpecialCandidates[0] = nonSpecialCandidates[letterIndex]
            nonSpecialCandidates[letterIndex] = temp
        }

        // Check that insertion is feasible.
        // We have nonSpecialCandidates.size available gaps (one after each non‑special element).
        if (specialCandidates.size > nonSpecialCandidates.size) {
            throw RuntimeException("Cannot arrange characters to satisfy no adjacent special rule")
        }

        // Determine which gaps will hold a special character.
        // The available gap positions are labeled 1..nonSpecialCandidates.size (i.e. after each ns element).
        val gapPositions = (1..nonSpecialCandidates.size).toMutableList()
        gapPositions.shuffle(secureRandom)
        val chosenGaps = gapPositions.take(specialCandidates.size).sorted()

        // Build the final password by taking a non‑special element and inserting specials in the chosen gaps.
        val finalList = mutableListOf<Char>()
        var specialIndex = 0
        for (i in nonSpecialCandidates.indices) {
            finalList.add(nonSpecialCandidates[i])
            // If the gap right after ns element at index i (i.e. gap position i+1) is chosen, insert one special.
            if ((i + 1) in chosenGaps && specialIndex < specialCandidates.size) {
                finalList.add(specialCandidates[specialIndex])
                specialIndex++
            }
        }
        // (If any special still remains, append it—this shouldn’t normally happen.)
        while (specialIndex < specialCandidates.size) {
            finalList.add(specialCandidates[specialIndex])
            specialIndex++
        }

        return finalList.joinToString("")
    }

}