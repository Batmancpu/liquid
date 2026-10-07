package com.mangoloads.expmango

import kotlin.math.min

object TypoEngine {
    private val qwertyNeighbors = mapOf(
        'q' to listOf('w', 'a', 's'),
        'w' to listOf('q', 'e', 'a', 's', 'd'),
        'e' to listOf('w', 'r', 's', 'd', 'f'),
        'r' to listOf('e', 't', 'd', 'f', 'g'),
        't' to listOf('r', 'y', 'f', 'g', 'h'),
        'y' to listOf('t', 'u', 'g', 'h', 'j'),
        'u' to listOf('y', 'i', 'h', 'j', 'k'),
        'i' to listOf('u', 'o', 'j', 'k', 'l'),
        'o' to listOf('i', 'p', 'k', 'l'),
        'p' to listOf('o', 'l'),
        'a' to listOf('q', 'w', 's', 'z'),
        's' to listOf('w', 'e', 'a', 'd', 'z', 'x'),
        'd' to listOf('e', 'r', 's', 'f', 'x', 'c'),
        'f' to listOf('r', 't', 'd', 'g', 'c', 'v'),
        'g' to listOf('t', 'y', 'f', 'h', 'v', 'b'),
        'h' to listOf('y', 'u', 'g', 'j', 'b', 'n'),
        'j' to listOf('u', 'i', 'h', 'k', 'n', 'm'),
        'k' to listOf('i', 'o', 'j', 'l', 'm'),
        'l' to listOf('o', 'p', 'k'),
        'z' to listOf('a', 's', 'x'),
        'x' to listOf('z', 's', 'd', 'c'),
        'c' to listOf('x', 'd', 'f', 'v'),
        'v' to listOf('c', 'f', 'g', 'b'),
        'b' to listOf('v', 'g', 'h', 'n'),
        'n' to listOf('b', 'h', 'j', 'm'),
        'm' to listOf('n', 'j', 'k')
    )

    fun damerauLevenshteinDistance(source: String, target: String): Int {
        val srcLen = source.length
        val tgtLen = target.length
        if (srcLen == 0) return tgtLen
        if (tgtLen == 0) return srcLen

        val dp = Array(srcLen + 1) { IntArray(tgtLen + 1) }

        for (i in 0..srcLen) dp[i][0] = i
        for (j in 0..tgtLen) dp[0][j] = j

        for (i in 1..srcLen) {
            for (j in 1..tgtLen) {
                val cost = if (source[i - 1].lowercaseChar() == target[j - 1].lowercaseChar()) 0 else 1
                dp[i][j] = min(
                    min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                )

                // Transposition
                if (i > 1 && j > 1 &&
                    source[i - 1].lowercaseChar() == target[j - 2].lowercaseChar() &&
                    source[i - 2].lowercaseChar() == target[j - 1].lowercaseChar()
                ) {
                    dp[i][j] = min(dp[i][j], dp[i - 2][j - 2] + cost)
                }
            }
        }

        return dp[srcLen][tgtLen]
    }

    fun isNeighborKey(a: Char, b: Char): Boolean {
        val neighbors = qwertyNeighbors[a.lowercaseChar()] ?: return false
        return neighbors.contains(b.lowercaseChar())
    }
}
