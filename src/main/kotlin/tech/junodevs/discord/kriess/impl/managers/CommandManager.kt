package tech.junodevs.discord.kriess.impl.managers

import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import tech.junodevs.discord.kriess.command.Command
import tech.junodevs.discord.kriess.command.CommandEvent
import tech.junodevs.discord.kriess.managers.GuildSettingsManager
import tech.junodevs.discord.kriess.managers.ICommandManager
import tech.junodevs.discord.kriess.providers.GuildSettingsProvider

class CommandManager<T : GuildSettingsProvider>(val guildSettingsManager: GuildSettingsManager<T>, override val defaultPrefix: String) : ICommandManager<T> {

    init {
        guildSettingsManager.start()

        Runtime.getRuntime().addShutdownHook(Thread {
            guildSettingsManager.shutdown()
        })
    }

    val commands: ArrayList<Command<T>> = arrayListOf()
    val owners: ArrayList<String> = arrayListOf()

    var mentionPrefixes: Array<String> = arrayOf()

    override fun onGuildMessage(event: GuildMessageReceivedEvent) {
        guildSettingsManager.getSettingsFor(event.guild).thenAccept { guildSettings ->
            if (event.author.isBot) return@thenAccept

            val raw = event.message.contentRaw
            val mentionPrefix = mentionPrefixes.find { raw.startsWith(it) }

            val remainder = when {
                // Mention prefix
                mentionPrefix != null -> raw.substring(mentionPrefix.length)
                raw.startsWith(guildSettings.realPrefix) -> raw.substring(guildSettings.realPrefix.length)
                else -> return@thenAccept
            }.trim()

            val split = remainder.split(" ")
            val commandLabel = split[0].toLowerCase()
            val args = split.slice(1 until split.size)

            val command = getCommand(commandLabel)
            if (command != null && command.ownerOnly && !owners.contains(event.author.id)) return@thenAccept
            if (command != null && command.preHandle(CommandEvent(event, command, owners.contains(event.author.id), args, guildSettingsManager, this)))
                command.handle(CommandEvent(event, command, owners.contains(event.author.id), args, guildSettingsManager, this))
        }
    }

    override fun onReadyEvent(event: ReadyEvent) {
        if (mentionPrefixes.isEmpty()) {
            mentionPrefixes = arrayOf(
                "<@${event.jda.selfUser.id}>",
                "<@!${event.jda.selfUser.id}>"
            )
        }
    }

    override fun addCommand(command: Command<T>) {
        if (!commands.contains(command)) { commands.add(command) }
    }

    override fun removeCommand(command: Command<T>) {
        commands.remove(command)
    }

    override fun getCommand(label: String): Command<T>? {
        return commands.find { it.name == label || it.aliases.contains(label) }
    }

    override fun addOwner(id: String) {
        if (!owners.contains(id)) { owners.add(id) }
    }

    override fun removeOwner(id: String) {
        owners.remove(id)
    }

    override fun isOwner(id: String): Boolean {
        return owners.contains(id)
    }

    override fun getCommands(): List<Command<T>> {
        return commands
    }

}