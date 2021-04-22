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

package tech.junodevs.discord.kriess.managers

import net.dv8tion.jda.api.entities.Guild
import tech.junodevs.discord.kriess.providers.GuildSettingsProvider
import java.util.concurrent.CompletableFuture

/**
 * The GuildSettingsManager interface, implemented by all GuildSettingsManagers
 */
interface GuildSettingsManager<T : GuildSettingsProvider> {

    /**
     * Retrieve the settings for a [Guild]
     */
    operator fun get(guild: Guild) = getSettingsFor(guild)

    /**
     * Retrieve the settings for a [Guild]
     */
    fun getSettingsFor(guild: Guild): CompletableFuture<T>

    /**
     * Edit the settings of a [Guild], with [action] being applied to the found settings object
     */
    fun editSettings(guild: Guild, action: T.() -> Unit)

    /**
     * Initialize the manager
     */
    fun start() {}

    /**
     * Save all of the data in the manager
     */
    fun save() {}

    /**
     * Shut down the manager
     */
    fun shutdown() {}
}