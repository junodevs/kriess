package tech.junodevs.discord.kriess.command

import tech.junodevs.discord.kriess.providers.GuildSettingsProvider

abstract class Command<T: GuildSettingsProvider>(
    val name: String,
    val aliases: List<String> = listOf(),
    val description: String,
    val usage: String = name,
    val category: CommandCategory,
    val showInHelp: Boolean = true,
    val ownerOnly: Boolean = false,
) {

    open fun preHandle(event: CommandEvent<T>): Boolean {
        return true
    }

    abstract fun handle(event: CommandEvent<T>)

}