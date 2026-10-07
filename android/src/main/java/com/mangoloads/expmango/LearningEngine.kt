package com.mangoloads.expmango

import android.content.Context
import java.util.concurrent.ConcurrentHashMap

object LearningEngine {
    private val unigramCounts = ConcurrentHashMap<String, Int>()
    private val bigramCounts = ConcurrentHashMap<String, MutableMap<String, Int>>()

    fun recordCommit(context: Context, word: String, previousWord: String?, policy: FieldPolicy) {
        if (!policy.isLearningEnabled) return

        val cleanWord = word.trim().lowercase()
        if (cleanWord.length <= 1) return

        unigramCounts[cleanWord] = (unigramCounts[cleanWord] ?: 0) + 1
        DictionaryManager.addUserWord(context, cleanWord)

        if (previousWord != null) {
            val cleanPrev = previousWord.trim().lowercase()
            if (cleanPrev.isNotEmpty()) {
                val nextMap = bigramCounts.computeIfAbsent(cleanPrev) { ConcurrentHashMap() }
                nextMap[cleanWord] = (nextMap[cleanWord] ?: 0) + 1
            }
        }
    }

    fun getBigramCandidates(previousWord: String?): List<String> {
        if (previousWord == null) return emptyList()
        val cleanPrev = previousWord.trim().lowercase()
        val nextMap = bigramCounts[cleanPrev] ?: return emptyList()
        return nextMap.entries.sortedByDescending { it.value }.map { it.key }.take(3)
    }

    fun getScore(word: String): Int {
        return unigramCounts[word.lowercase()] ?: 0
    }
}
