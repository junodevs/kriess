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

package tech.junodevs.discord.kriess.command

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import tech.junodevs.discord.kriess.command.arguments.Argument
import tech.junodevs.discord.kriess.command.arguments.ArgumentResult
import tech.junodevs.discord.kriess.impl.managers.CommandManager
import tech.junodevs.discord.kriess.managers.GuildSettingsManager
import tech.junodevs.discord.kriess.managers.ICommandManager
import tech.junodevs.discord.kriess.providers.GuildSettingsProvider

class CommandEvent(
    val event: GuildMessageReceivedEvent,
    val command: Command,
    val isOwner: Boolean,
    val args: String,
    val settingsManager: GuildSettingsManager<*>,
    val commandManager: ICommandManager,
) {

    val author: User
        get() = event.author
    val channel: MessageChannel
        get() = event.channel
    val guild: Guild
        get() = event.guild
    val jda: JDA
        get() = event.jda
    val member: Member
        get() = event.member!!
    val message: Message
        get() = event.message
    val selfMember: Member
        get() = guild.selfMember
    val selfUser: SelfUser
        get() = jda.selfUser
    val textChannel: TextChannel
        get() = event.channel

    val arguments: ArgumentResult by lazy { Argument.parse(this, command.arguments) }

    fun reply(message: String, success: ((Message) -> Unit)? = null, failure: ((Throwable) -> Unit)? = null) {
        event.channel.sendMessage(message).queue(success, failure)
    }

    fun reply(embed: MessageEmbed, success: ((Message) -> Unit)? = null, failure: ((Throwable) -> Unit)? = null) {
        event.channel.sendMessage(embed).queue(success, failure)
    }

    fun replyError(message: String, success: ((Message) -> Unit)? = null, failure: ((Throwable) -> Unit)? = null) {
        reply(":x: $message", success, failure)
    }

}