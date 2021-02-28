package tech.junodevs.discord.kriess.services

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

// Set an initial of 0 for it to run when the service is begun,
// otherwise defaults to a delay that is the same as the period
abstract class Service(private val period: Long, private val initial: Long = period) {

    val isRunning: Boolean
        get() = !(task?.isDone ?: true)

    private var task: ScheduledFuture<*>? = null

    protected abstract fun execute()

    open fun start() = beginTask()
    open fun shutdown() {
        task?.cancel(false)
    }

    private fun beginTask() {
        if (isRunning) shutdown()
        task = executor.scheduleWithFixedDelay(::execute, initial, period, TimeUnit.SECONDS)
    }

    companion object {
        // Services aren't time critical, if two run at the same time, one can wait
        private val executor =
            Executors.newSingleThreadScheduledExecutor(CountingThreadFactory("service"))
    }

}
