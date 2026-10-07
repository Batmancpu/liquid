package com.mangoloads.expmango

object PredictionEngine {
    fun getPredictions(
        currentWord: String,
        previousWord: String?,
        policy: FieldPolicy
    ): List<String> {
        if (!policy.isPredictionEnabled) return emptyList()

        // 1. Email domain suggestion handling
        if (policy.isEmailDomainMode && currentWord.contains('@')) {
            val domains = EmailDomainProvider.getSuggestions(currentWord)
            if (domains.isNotEmpty()) {
                val prefix = currentWord.substring(0, currentWord.lastIndexOf('@') + 1)
                return domains.map { prefix + it }
            }
        }

        val trimmed = currentWord.trim()

        // 2. Next word prediction if current word is empty
        if (trimmed.isEmpty()) {
            return LearningEngine.getBigramCandidates(previousWord)
        }

        // 3. Prefix matching
        val prefixMatches = DictionaryManager.getPrefixMatches(trimmed, 5)

        // 4. Typo correction if no prefix matches and autocorrect is permitted
        val results = mutableListOf<String>()
        results.addAll(prefixMatches)

        if (results.isEmpty() && policy.isAutocorrectEnabled && trimmed.length >= 3) {
            val allWords = DictionaryManager.getAllWords()
            val corrections = allWords.filter { word ->
                kotlin.math.abs(word.length - trimmed.length) <= 1 &&
                TypoEngine.damerauLevenshteinDistance(trimmed, word) <= 1
            }.take(3)
            results.addAll(corrections)
        }

        return results.distinct().take(3)
    }
}
