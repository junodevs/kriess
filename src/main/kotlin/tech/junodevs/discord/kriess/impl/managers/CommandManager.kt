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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import tech.junodevs.discord.kriess.command.Command
import tech.junodevs.discord.kriess.command.CommandEvent
import tech.junodevs.discord.kriess.events.EventWaiter
import tech.junodevs.discord.kriess.managers.GuildSettingsManager
import tech.junodevs.discord.kriess.managers.ICommandManager
import tech.junodevs.discord.kriess.providers.GuildSettingsProvider
import tech.junodevs.discord.kriess.utils.splitSpaces
import kotlin.coroutines.EmptyCoroutineContext

class CommandManager<T : GuildSettingsProvider>(val guildSettingsManager: GuildSettingsManager<T>, override val defaultPrefix: String) : ICommandManager {

    private val scope = CoroutineScope(EmptyCoroutineContext)

    init {
        guildSettingsManager.start()

        Runtime.getRuntime().addShutdownHook(Thread {
            guildSettingsManager.shutdown()
        })
    }

    val commands: ArrayList<Command> = arrayListOf()
    val owners: ArrayList<String> = arrayListOf()
    override val eventWaiter: EventWaiter = EventWaiter()

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

            val parts = remainder.splitSpaces(2)
            val commandLabel = parts[0].toLowerCase()

            val command = getCommand(commandLabel) ?: return@thenAccept
            val args = if (parts.size == 1) "" else parts[1]
            val cEvent =
                CommandEvent(event, command, owners.contains(event.author.id), args, guildSettingsManager, this)

            scope.launch {
                if (command.preHandle(cEvent))
                    command.handle(cEvent)
            }
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

    override fun addCommand(command: Command) {
        if (!commands.contains(command)) { commands.add(command) }
    }

    override fun removeCommand(command: Command) {
        commands.remove(command)
    }

    override fun getCommand(label: String): Command? {
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

    override fun getCommands(): List<Command> {
        return commands
    }

}