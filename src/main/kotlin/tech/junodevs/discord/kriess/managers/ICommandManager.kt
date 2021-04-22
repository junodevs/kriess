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

import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.EventListener
import tech.junodevs.discord.kriess.command.Command
import tech.junodevs.discord.kriess.command.CommandEvent
import tech.junodevs.discord.kriess.events.EventWaiter

/**
 * The CommandManager interface, implemented by all CommandManagers.
 */
interface ICommandManager: EventListener {

    /**
     * The [defaultPrefix] for the bot
     */
    val defaultPrefix: String

    /**
     * The associated [EventWaiter]
     */
    val eventWaiter: EventWaiter

    /**
     * Called by JDA
     */
    override fun onEvent(event: GenericEvent) {
        when (event) {
            is GuildMessageReceivedEvent -> onGuildMessage(event)
            is ReadyEvent -> onReadyEvent(event)
        }
    }

    /**
     * A way to hook into messages before they make it to a command
     * Return false in order to prevent a message from being parsed
     */
    fun messageHook(event: GuildMessageReceivedEvent): Boolean

    /**
     * The function to run when a [Command] errors
     */
    fun onCommandError(event: CommandEvent, throwable: Throwable)

    /**
     * Called when a [GenericEvent] in [ICommandManager.onEvent] is a [GuildMessageReceivedEvent]
     */
    fun onGuildMessage(event: GuildMessageReceivedEvent)

    /**
     * Called when a [GenericEvent] in [ICommandManager.onEvent] is a [ReadyEvent]
     */
    fun onReadyEvent(event: ReadyEvent)

    /**
     * Register a owner in this [ICommandManager]
     */
    fun addOwner(id: String)

    /**
     * Remove a owner from this [ICommandManager]
     */
    fun removeOwner(id: String)

    /**
     * Check if a given [id] is an owner in this [ICommandManager]
     */
    fun isOwner(id: String): Boolean

    /**
     * Add a [command] to this [ICommandManager]
     */
    fun addCommand(command: Command)

    /**
     * Remove a [command] from this [ICommandManager]
     */
    fun removeCommand(command: Command)

    /**
     * Get a [Command] with [label] from this [ICommandManager]
     */
    fun getCommand(label: String): Command?

    /**
     * Get all of the [Command]s registered with this [ICommandManager]
     */
    fun getCommands(): List<Command>

}