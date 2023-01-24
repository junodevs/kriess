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
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import tech.junodevs.discord.kriess.command.Command
import tech.junodevs.discord.kriess.command.CommandEvent
import tech.junodevs.discord.kriess.events.EventWaiter
import tech.junodevs.discord.kriess.managers.GuildSettingsManager
import tech.junodevs.discord.kriess.managers.ICommandManager
import tech.junodevs.discord.kriess.providers.GuildSettingsProvider
import tech.junodevs.discord.kriess.utils.splitSpaces
import kotlin.coroutines.EmptyCoroutineContext

/**
 * A bog standard implementation of [ICommandManager]
 */
class CommandManager<T : GuildSettingsProvider>(
    /**
     * The [GuildSettingsManager] assigned to this [CommandManager]
     */
    val guildSettingsManager: GuildSettingsManager<T>,
    /**
     * The [defaultPrefix] to be used when a guild doesn't have one
     */
    override val defaultPrefix: String,
    /**
     * The [preCommandHook] - great for auto-mod events. Is called before any message parsing occurs
     * Return false to halt further parsing of the event
     */
    private val preCommandHook: ((MessageReceivedEvent) -> Boolean) = { true },
    /**
     * The [errorHandler] for this [CommandManager]
     */
    private val errorHandler: ((CommandEvent, Throwable) -> Unit)? = null
) : ICommandManager {

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

    override fun messageHook(event: MessageReceivedEvent): Boolean {
        return preCommandHook(event)
    }

    override fun onMessage(event: MessageReceivedEvent) {
        if (!event.isFromGuild)
            return

        onGuildMessage(event)
    }

    override fun onGuildMessage(event: MessageReceivedEvent) {
        if (!messageHook(event))
            return

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
            val commandLabel = parts[0].lowercase()

            val command = getCommand(commandLabel) ?: return@thenAccept
            var args = if (parts.size == 1) "" else parts[1]

            val maybeSubcommand = if (args.isNotEmpty()) {
                command.findChild(args).also { args = it.second }.first ?: command
            } else command

            val cEvent =
                CommandEvent(event, maybeSubcommand, owners.contains(event.author.id), args, guildSettingsManager, this)

            scope.launch {
                try {
                    if (maybeSubcommand.preHandle(cEvent))
                        maybeSubcommand.handle(cEvent)
                } catch (t: Throwable) {
                    onCommandError(cEvent, t)
                }
            }
        }
    }

    override fun onCommandError(event: CommandEvent, throwable: Throwable) {
        errorHandler?.invoke(event, throwable)
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