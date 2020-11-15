package tech.junodevs.discord.kriess.command

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import tech.junodevs.discord.kriess.managers.GuildSettingsManager
import tech.junodevs.discord.kriess.providers.GuildSettingsProvider

class CommandEvent<T: GuildSettingsProvider>(
    val event: GuildMessageReceivedEvent,
    val command: Command<T>,
    val isOwner: Boolean,
    val args: List<String>,
    val settingsManager: GuildSettingsManager<T>
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