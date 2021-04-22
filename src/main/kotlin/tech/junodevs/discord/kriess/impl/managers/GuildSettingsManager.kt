/*
 * MIT License
 *
 * Copyright (c) 2020-2021 Juno Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package tech.junodevs.discord.kriess.impl.managers

import net.dv8tion.jda.api.entities.Guild
import org.yaml.snakeyaml.Yaml
import tech.junodevs.discord.kriess.managers.GuildSettingsManager
import tech.junodevs.discord.kriess.providers.GuildSettingsProvider
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.CompletableFuture

/**
 * An implementation of [GuildSettingsManager], using YAML as a storage medium
 */
abstract class GuildSettingsManager<T : GuildSettingsProvider> : GuildSettingsManager<T> {

    private val yaml = Yaml()

    private val file = File("guild-settings.yml")

    private var guildSettings: MutableMap<Long, T> = mutableMapOf()

    /**
     * Start up the settings manager
     */
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

    /**
     * Save the settings to disk
     */
    override fun save() {
        file.delete()
        file.writeText(
            yaml.dumpAsMap(
                guildSettings.mapValues { it.value.toMap() }
            )
        )
    }

    /**
     * Shut down the settings manager
     */
    override fun shutdown() {
        save()
    }

    /**
     * Retrieve the settings for a [Guild]
     */
    override fun getSettingsFor(guild: Guild): CompletableFuture<T> {
        val future = CompletableFuture<T>()

        future.complete(guildSettings.computeIfAbsent(guild.idLong) { createAbsentInstance(it) })

        return future
    }

    /**
     * Edit the settings of a [Guild]
     */
    override fun editSettings(guild: Guild, action: T.() -> Unit) {
        getSettingsFor(guild).thenAccept {
            action.invoke(it)
            save()
        }
    }

    /**
     * Create an instance of [T] when the settings are already on-file
     */
    abstract fun createInstance(guildId: Long, properties: Map<String, Any?>): T

    /**
     * Create a fresh instance of [T]; used when the settings are not on-file
     */
    abstract fun createAbsentInstance(guildId: Long): T

}