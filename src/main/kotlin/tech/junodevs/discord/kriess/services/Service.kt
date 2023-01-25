package tech.junodevs.discord.kriess.services

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.seconds

/**
 * Represents a service that runs every [period] seconds.
 */
abstract class Service(
    /**
     * The [period] (number of seconds) between each execution
     */
    private val period: Long,

    /**
     * The [initial] delay before the service is executed for the first time
     */
    private val initial: Long = period
) {

    /**
     * Is the task running right now?
     */
    val isActive: Boolean
        get() = job?.isActive ?: false

    private var job: Job? = null

    /**
     * The task that should be executed every [period] seconds
     */
    protected abstract suspend fun execute()

    /**
     * Start the service running, executing the task every [period] seconds.
     * If the service is already running, this does nothing.
     */
    open fun start() {
        if (isActive) return
        serviceScope.launch {
            delay(initial.seconds)
            while (isActive) try {
                execute()
                delay(period.seconds)
            } catch (t: Throwable) {
                logger.error("An exception occurred while executing a service task, halting service", t)
                shutdown()
            }
        }
    }

    /**
     * Stop the service and cancel the task
     */
    open fun shutdown() {
        job?.cancel("Service shutdown")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Service::class.java)
        internal val serviceScope = CoroutineScope(Dispatchers.Default)
    }

}
