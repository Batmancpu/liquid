package com.mangoloads.expmango

import android.content.Context

object DictionaryManager {
    private val englishWords = listOf(
        "the", "be", "to", "of", "and", "a", "in", "that", "have", "i",
        "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
        "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",
        "or", "an", "will", "my", "one", "all", "would", "there", "their", "what",
        "so", "up", "out", "if", "about", "who", "get", "which", "go", "me",
        "when", "make", "can", "like", "time", "no", "just", "him", "know", "take",
        "people", "into", "year", "your", "good", "some", "could", "them", "see", "other",
        "than", "then", "now", "look", "only", "come", "its", "over", "think", "also",
        "back", "after", "use", "two", "how", "our", "work", "first", "well", "way",
        "even", "new", "want", "because", "any", "these", "give", "day", "most", "us",
        "going", "really", "please", "thanks", "tomorrow", "tonight", "morning", "hello"
    )

    private val hinglishWords = listOf(
        "kya", "kyun", "kaise", "hai", "hain", "acha", "achha", "bahut", "nahi", "nahin",
        "mujhe", "tumhe", "aap", "bhai", "yaar", "theek", "shukriya", "chalo", "ab", "kab",
        "kahan", "idhar", "udhar", "baat", "karo", "karna", "hua", "raha", "rahi", "bolo",
        "sun", "samajh", "aaya", "jao", "dekh", "lekin", "aur", "sab", "kuch", "aisa"
    )

    private val userWords = mutableSetOf<String>()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("mango_dict", Context.MODE_PRIVATE)
        val saved = prefs.getStringSet("user_words", emptySet())
        if (saved != null) {
            userWords.addAll(saved)
        }
    }

    fun isKnownWord(word: String): Boolean {
        val lower = word.lowercase()
        return englishWords.contains(lower) || hinglishWords.contains(lower) || userWords.contains(lower)
    }

    fun getPrefixMatches(prefix: String, limit: Int = 10): List<String> {
        val lower = prefix.lowercase()
        val results = mutableListOf<String>()

        // 1. User learned words first
        for (w in userWords) {
            if (w.startsWith(lower)) results.add(w)
            if (results.size >= limit) return results
        }

        // 2. Hinglish words
        for (w in hinglishWords) {
            if (w.startsWith(lower) && !results.contains(w)) results.add(w)
            if (results.size >= limit) return results
        }

        // 3. English words
        for (w in englishWords) {
            if (w.startsWith(lower) && !results.contains(w)) results.add(w)
            if (results.size >= limit) return results
        }

        return results
    }

    fun getAllWords(): List<String> {
        return userWords.toList() + hinglishWords + englishWords
    }

    fun addUserWord(context: Context, word: String) {
        val lower = word.lowercase()
        if (lower.length > 1 && !isKnownWord(lower)) {
            userWords.add(lower)
            val prefs = context.getSharedPreferences("mango_dict", Context.MODE_PRIVATE)
            prefs.edit().putStringSet("user_words", userWords).apply()
        }
    }
}
