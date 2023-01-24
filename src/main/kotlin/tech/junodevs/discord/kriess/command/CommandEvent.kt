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
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import tech.junodevs.discord.kriess.command.arguments.Argument
import tech.junodevs.discord.kriess.command.arguments.ArgumentResult
import tech.junodevs.discord.kriess.events.EventWaiter
import tech.junodevs.discord.kriess.managers.GuildSettingsManager
import tech.junodevs.discord.kriess.managers.ICommandManager

/**
 * A representation of a [Command] being triggered
 */
class CommandEvent(
        /**
         * The [MessageReceivedEvent] that triggered this [CommandEvent]
         */
        val event: MessageReceivedEvent,

        /**
         * The [Command] that was chosen by the [ICommandManager] to handle this event
         */
        val command: Command,

        /**
         * Is the [author] registered as an owner in the [commandManager] that handled this event
         */
        val isOwner: Boolean,

        /**
         * Everything from the [Message.getContentRaw] except the command slug and prefix
         */
        val args: String,

        /**
         * The [GuildSettingsManager] assigned to the [commandManager] that handled this event
         */
        val settingsManager: GuildSettingsManager<*>,

        /**
         * The [ICommandManager] that handled this event
         */
        val commandManager: ICommandManager,
) {

    /**
     * The [User] that sent the event
     */
    val author: User
        get() = event.author

    /**
     * The [MessageChannel] the event was sent in
     */
    val channel: MessageChannel
        get() = event.channel

    /**
     * The [Guild] the event was sent in
     */
    val guild: Guild
        get() = event.guild

    /**
     * The [JDA] instance that it was triggered on
     */
    val jda: JDA
        get() = event.jda

    /**
     * The [Member] that sent the event
     */
    val member: Member
        get() = event.member!!

    /**
     * The [Message] that triggered the event
     */
    val message: Message
        get() = event.message

    /**
     * The [Member] representing the bot in the [guild]
     */
    val selfMember: Member
        get() = guild.selfMember

    /**
     * The currently logged in account
     */
    val selfUser: SelfUser
        get() = jda.selfUser

    /**
     * The [TextChannel] the [message] was sent in
     */
    val textChannel: GuildMessageChannel
        get() = event.guildChannel

    /**
     * The [EventWaiter] of the [ICommandManager] that handled this event
     */
    val eventWaiter: EventWaiter
        get() = commandManager.eventWaiter

    /**
     * The [ArgumentResult] that was parsed from the [CommandEvent] and [Command.arguments]
     */
    val arguments: ArgumentResult by lazy { Argument.parse(command.arguments, message, args, commandManager) }

    /**
     * Sends the [message] to the [channel] the event was triggered in.
     * Calls [success] and [failure] respectively
     */
    fun reply(message: MessageCreateData, success: ((Message) -> Unit)? = {}, failure: ((Throwable) -> Unit)? = {}) {
        event.channel.sendMessage(message).queue(success, failure)
    }

    /**
     * Sends the [message] to the [channel] the event was triggered in.
     * Calls [success] and [failure] respectively
     */
    fun reply(message: String, success: ((Message) -> Unit)? = {}, failure: ((Throwable) -> Unit)? = {}) {
        reply(MessageCreateBuilder().setContent(message).build(), success, failure)
    }

    /**
     * Sends the [message] to the [channel] the event was triggered in.
     * Calls [success] and [failure] respectively
     */
    fun reply(embed: MessageEmbed, success: ((Message) -> Unit)? = {}, failure: ((Throwable) -> Unit)? = {}) {
        reply(MessageCreateBuilder().setEmbeds(embed).build(), success, failure)
    }

    /**
     * Sends the [message] prefixed with ":x:" to the [channel] the event was triggered in.
     * Calls [success] and [failure] respectively
     */
    fun replyError(message: String, success: ((Message) -> Unit)? = {}, failure: ((Throwable) -> Unit)? = {}) {
        reply(":x: $message", success, failure)
    }

}