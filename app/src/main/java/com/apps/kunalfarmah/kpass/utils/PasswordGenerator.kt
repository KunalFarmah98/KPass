import com.apps.kunalfarmah.kpass.BuildConfig
import java.security.SecureRandom

object PasswordGenerator {

    val availablePasswordLengths = listOf<Int>(8, 15, 20, 25, 30)

    fun generateSecurePassword(length: Int): String {
        val lowerPool = BuildConfig.LOWERCASE_POOL
        val upperPool = BuildConfig.UPPERCASE_POOL
        val digitsPool = BuildConfig.DIGITS_POOL
        val specialPool = BuildConfig.SPECIAL_POOL

        val maxUnique = 26 + digitsPool.length + specialPool.length

        if (length > maxUnique) {
            throw IllegalArgumentException("Requested length $length exceeds maximum unique characters: $maxUnique")
        }

        val secureRandom = SecureRandom()
        val candidates = mutableListOf<Char>()
        val usedLetters = mutableSetOf<Char>()

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

        fun addDigit(count: Int) {
            val available = digitsPool.toList().filter { ch -> ch !in candidates }
            if (available.size < count) {
                throw IllegalArgumentException("Not enough unique digits available")
            }
            available.shuffled(secureRandom).take(count).forEach {
                candidates.add(it)
            }
        }

        fun addSpecial(count: Int) {
            val available = specialPool.toList().filter { ch -> ch !in candidates }
            if (available.size < count) {
                throw IllegalArgumentException("Not enough unique special characters available")
            }
            available.shuffled(secureRandom).take(count).forEach {
                candidates.add(it)
            }
        }

        if (length > 8) {
            addLetter(lowerPool, BuildConfig.MIN_LOWER)
            addLetter(upperPool, BuildConfig.MIN_UPPER)
            addDigit(BuildConfig.MIN_DIGITS)
            addSpecial(BuildConfig.MIN_SPECIAL)
            val mandatoryCount = BuildConfig.MANDATORY_CHARS
            val extraCount = length - mandatoryCount

            for (i in 0 until extraCount) {
                val availableChars = mutableListOf<Char>()
                availableChars.addAll(lowerPool.toList().filter { it !in candidates && !usedLetters.contains(it.lowercaseChar()) })
                availableChars.addAll(upperPool.toList().filter { it !in candidates && !usedLetters.contains(it.lowercaseChar()) })
                availableChars.addAll(digitsPool.toList().filter { it !in candidates })
                availableChars.addAll(specialPool.toList().filter { it !in candidates })

                if (availableChars.isEmpty()) break

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

        val nonSpecialCandidates = candidates.filter { !specialPool.contains(it) }.toMutableList()
        val specialCandidates = candidates.filter { specialPool.contains(it) }.toMutableList()

        nonSpecialCandidates.shuffle(secureRandom)
        specialCandidates.shuffle(secureRandom)

        if (nonSpecialCandidates.isEmpty() || nonSpecialCandidates.none { it.isLetter() }) {
            throw RuntimeException("No letter available to start the password")
        }
        if (!nonSpecialCandidates[0].isLetter()) {
            val letterIndex = nonSpecialCandidates.indexOfFirst { it.isLetter() }
            val temp = nonSpecialCandidates[0]
            nonSpecialCandidates[0] = nonSpecialCandidates[letterIndex]
            nonSpecialCandidates[letterIndex] = temp
        }


        if (specialCandidates.size > nonSpecialCandidates.size) {
            throw RuntimeException("Cannot arrange characters to satisfy no adjacent special rule")
        }

        val gapPositions = (1..nonSpecialCandidates.size).toMutableList()
        gapPositions.shuffle(secureRandom)
        val chosenGaps = gapPositions.take(specialCandidates.size).sorted()

        val finalList = mutableListOf<Char>()
        var specialIndex = 0
        for (i in nonSpecialCandidates.indices) {
            finalList.add(nonSpecialCandidates[i])
            if ((i + 1) in chosenGaps && specialIndex < specialCandidates.size) {
                finalList.add(specialCandidates[specialIndex])
                specialIndex++
            }
        }

        while (specialIndex < specialCandidates.size) {
            finalList.add(specialCandidates[specialIndex])
            specialIndex++
        }

        return finalList.joinToString("")
    }

}