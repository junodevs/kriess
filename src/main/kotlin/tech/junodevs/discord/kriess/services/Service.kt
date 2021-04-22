package tech.junodevs.discord.kriess.services

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * Represents a service that runs every [period] seconds.
 */
abstract class Service(
    /**
     * The [period] (number of seconds) between each execution
     */
    val period: Long,

    /**
     * The [initial] delay before the service is executed for the first time
     */
    val initial: Long = period
) {

    /**
     * Is the task running right now?
     */
    val isRunning: Boolean
        get() = !(task?.isDone ?: true)

    private var task: ScheduledFuture<*>? = null

    /**
     * The task that should be executed every [period] seconds
     */
    protected abstract fun execute()

    /**
     * Start the task
     */
    open fun start() = beginTask()

    /**
     * Stop the task
     */
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
