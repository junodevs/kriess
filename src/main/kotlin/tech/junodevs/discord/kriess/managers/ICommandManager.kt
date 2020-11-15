package tech.junodevs.discord.kriess.managers

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.EventListener
import tech.junodevs.discord.kriess.command.Command
import tech.junodevs.discord.kriess.providers.GuildSettingsProvider

interface ICommandManager<T: GuildSettingsProvider> : EventListener {

    val defaultPrefix: String

     override fun onEvent(event: GenericEvent) {
        when (event) {
            is GuildMessageReceivedEvent -> onGuildMessage(event)
            is ReadyEvent -> onReadyEvent(event)
        }
    }

    fun onGuildMessage(event: GuildMessageReceivedEvent)

    fun onReadyEvent(event: ReadyEvent)

    fun addOwner(id: String)

    fun removeOwner(id: String)

    fun isOwner(id: String): Boolean

    fun addCommand(command: Command<T>)

    fun removeCommand(command: Command<T>)

    fun getCommand(label: String): Command<T>?

}