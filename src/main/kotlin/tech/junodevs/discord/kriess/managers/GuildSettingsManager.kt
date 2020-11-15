package tech.junodevs.discord.kriess.managers

import net.dv8tion.jda.api.entities.Guild
import tech.junodevs.discord.kriess.providers.GuildSettingsProvider
import java.util.concurrent.CompletableFuture

interface GuildSettingsManager<T : GuildSettingsProvider> {

    operator fun get(guild: Guild) = getSettingsFor(guild)

    fun getSettingsFor(guild: Guild): CompletableFuture<T>

    fun editSettings(guild: Guild, action: T.() -> Unit)

    fun start() {}

    fun save() {}

    fun shutdown() {}

}