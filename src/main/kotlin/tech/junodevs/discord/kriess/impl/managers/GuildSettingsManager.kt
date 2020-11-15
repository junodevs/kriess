package tech.junodevs.discord.kriess.impl.managers

import net.dv8tion.jda.api.entities.Guild
import org.yaml.snakeyaml.Yaml
import tech.junodevs.discord.kriess.managers.GuildSettingsManager
import tech.junodevs.discord.kriess.providers.GuildSettingsProvider
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.CompletableFuture

abstract class GuildSettingsManager<T : GuildSettingsProvider> : GuildSettingsManager<T> {

    val yaml = Yaml()

    private val file = File("guild-settings.yml")

    private var guildSettings: MutableMap<Long, T> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    override fun start() {
        if (!file.exists()) {
            // Don't have anything to load D:
            return
        }

        val raw = yaml.load(FileInputStream(file)) as Map<Long, Any>

        guildSettings.clear()

        raw.mapKeys { it.key }
            .mapValues {
                try {
                    val section = it.value as Map<String, Any>

                    return@mapValues createInstance(it.key, section)
                } catch (e: Exception) {
                    println("Failed to load guild settings for guild with ID '${it.key}'")
                    e.printStackTrace()
                    return@mapValues null
                }
            }
            .filterValues { it != null }
            .mapValues { it.value!! }
            .forEach { guildSettings[it.key] = it.value }
    }

    override fun save() {
        file.delete()
        file.writeText(
            yaml.dumpAsMap(
                guildSettings.mapValues { it.value.toMap() }
            )
        )
    }

    override fun shutdown() {
        save()
    }

    override fun getSettingsFor(guild: Guild): CompletableFuture<T> {
        val future = CompletableFuture<T>()

        future.complete(guildSettings.computeIfAbsent(guild.idLong) { createAbsentInstance(it) })

        return future
    }

     override fun editSettings(guild: Guild, action: T.() -> Unit) {
        getSettingsFor(guild).thenAccept {
            action.invoke(it)
            save()
        }
    }

    abstract fun createInstance(guildId: Long, properties: Map<String, Any?>): T

    abstract fun createAbsentInstance(guildId: Long): T

}