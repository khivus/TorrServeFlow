package io.github.khivus.torrserveflow.utils

import io.github.khivus.torrserveflow.ext.clearName

data class TorrentFileLabel(val title: String, val folder: String)

object TorrentFileLabels {
    fun create(sharedTitle: String, paths: List<String>): List<TorrentFileLabel> {
        if (paths.isEmpty()) return emptyList()
        val normalizedPaths = paths.map { it.replace('\\', '/') }
        val parents = normalizedPaths.map { it.substringBeforeLast('/', "") }
        val parentParts = parents.map { if (it.isEmpty()) emptyList() else it.split('/') }
        val commonDirectory = commonPrefix(parentParts).joinToString("/")
        val stems = normalizedPaths.map { path ->
            val name = path.substringAfterLast('/')
            val dot = name.lastIndexOf('.')
            if (dot > 0) name.substring(0, dot) else name
        }
        val names = stems.map { it.clearName().split(' ').filter(String::isNotEmpty) }
        val header = sharedTitle.clearName().split(' ').filter(String::isNotEmpty)
        val prefix = commonPrefix(names)
        val suffix = commonPrefix(names.map { it.reversed() }).reversed()
        // Strip only shared context that is already visible in the heading.
        val prefixSize = if (paths.size > 1 && containsWords(header, prefix)) prefix.size else 0
        val suffixSize = if (paths.size > 1 && containsWords(header, suffix)) suffix.size else 0
        val labels = names.mapIndexed { index, words ->
            var start = prefixSize
            val end = words.size - suffixSize
            var title = if (start < end) words.subList(start, end).joinToString(" ") else ""
            // Keep the word before a standalone number (e.g. "Episode 01").
            if (start > 0 && title.isNotEmpty() && title.all { it.isDigit() || it.isWhitespace() }) {
                start--
                title = words.subList(start, end).joinToString(" ")
            }
            val folder = if (commonDirectory.isEmpty()) parents[index]
            else parents[index].removePrefix(commonDirectory).trimStart('/')
            TorrentFileLabel(title.ifBlank { stems[index].clearName() }, folder)
        }
        // Formatting must not conceal differences such as dots versus underscores.
        val duplicates = labels.withIndex().groupBy { it.value }.values
            .filter { group -> group.map { stems[it.index] }.distinct().size > 1 }
            .flatMap { group -> group.map { it.index } }.toSet()
        return labels.mapIndexed { index, label ->
            if (index in duplicates) label.copy(title = stems[index]) else label
        }
    }

    private fun commonPrefix(values: List<List<String>>): List<String> {
        val first = values.firstOrNull() ?: return emptyList()
        var count = 0
        while (count < first.size && values.all { it.getOrNull(count) == first[count] }) count++
        return first.take(count)
    }

    private fun containsWords(words: List<String>, shared: List<String>): Boolean {
        if (shared.isEmpty() || shared.size > words.size) return false
        return (0..words.size - shared.size).any { start ->
            shared.indices.all { words[start + it].equals(shared[it], ignoreCase = true) }
        }
    }
}
