package tech.junodevs.discord.kriess.services

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicLong

/**
 * A thread factory
 */
class CountingThreadFactory(private val identifier: String) : ThreadFactory {

    private val poolId: Long
    private var threadCount = AtomicLong(0)

    init {
        synchronized(identifiers) {
            val count = identifiers.computeIfAbsent(identifier) { AtomicLong(0) }
            poolId = count.getAndIncrement()
        }
    }

    override fun newThread(runnable: Runnable): Thread {
        val thread = Thread(runnable, "$identifier-$poolId-thread-${threadCount.incrementAndGet()}")
        thread.isDaemon = true
        return thread
    }

    companion object {
        private val identifiers = mutableMapOf<String, AtomicLong>()
    }

}