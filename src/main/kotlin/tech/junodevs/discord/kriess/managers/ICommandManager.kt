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

interface ICommandManager: EventListener {

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

    fun addCommand(command: Command)

    fun removeCommand(command: Command)

    fun getCommand(label: String): Command?

    fun getCommands(): List<Command>

}