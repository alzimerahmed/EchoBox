package com.alzimer.echobox.data.cache

import kotlin.time.TimeSource

/**
 * Process-lifetime, TTL-bounded read cache for expensive browse calls.
 *
 * Repositories are Koin singletons, so an instance lives for the whole session: reopening an
 * album/artist/podcast page inside the TTL serves the parsed model without touching the network,
 * while process death (or expiry) bounds how stale an answer can get. Entries evict by TTL on
 * read and by insertion order past [maxSize], so the map cannot grow without bound.
 *
 * Monotonic marks rather than wall-clock timestamps, so an NTP sync or a user-set clock can
 * neither expire an entry early nor stretch one past its TTL.
 *
 * JVM-only: access-order `LinkedHashMap` + `removeEldestEntry` + `@Synchronized` have no common
 * or Native equivalent. `:data` currently declares only the `android` target — if ios/jvm targets
 * ever come back this needs an `expect`/`actual` split.
 */
internal class TtlCache<K : Any, V : Any>(
    private val ttlMillis: Long,
    private val maxSize: Int = 64,
) {
    private class Entry<V>(val value: V, val mark: TimeSource.Monotonic.ValueTimeMark)

    private val entries = object : LinkedHashMap<K, Entry<V>>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, Entry<V>>?): Boolean =
            size > maxSize
    }

    @Synchronized
    fun get(key: K): V? {
        val entry = entries[key] ?: return null
        if (entry.mark.elapsedNow().inWholeMilliseconds > ttlMillis) {
            entries.remove(key)
            return null
        }
        return entry.value
    }

    @Synchronized
    fun put(key: K, value: V) {
        entries[key] = Entry(value, TimeSource.Monotonic.markNow())
    }
}
