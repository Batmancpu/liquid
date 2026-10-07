package com.mangoloads.expmango

object EmailDomainProvider {
    private val builtInDomains = listOf(
        "gmail.com",
        "outlook.com",
        "yahoo.com",
        "icloud.com",
        "proton.me",
        "protonmail.com",
        "hotmail.com"
    )

    fun getSuggestions(query: String): List<String> {
        val atIndex = query.lastIndexOf('@')
        if (atIndex == -1) return emptyList()

        val prefix = query.substring(atIndex + 1).lowercase()
        return if (prefix.isEmpty()) {
            builtInDomains.take(3)
        } else {
            builtInDomains.filter { it.startsWith(prefix) }
        }
    }
}
