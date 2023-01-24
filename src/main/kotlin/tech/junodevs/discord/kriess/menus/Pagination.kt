/*
 * MIT License
 *
 * Copyright (c) 2020-2023 Juno Developers
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

package tech.junodevs.discord.kriess.menus

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.MessageReaction
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * A simple paginator menu that goes through pages of fields
 */
class PaginatorMenu(
        /**
         * The [TextChannel] the paginator should be active in
         */
        private val channel: TextChannel,
        /**
         * The [User] that the paginator is to be used by
         */
        private val user: User,
        /**
         * The [title] of the menu
         */
        private val title: String,
        /**
         * The [PaginationOptions] for the menu
         */
        private val options: PaginationOptions,
        /**
         * The [pages] of the menu, with each sub-list being a page
         */
        private val pages: List<List<MessageEmbed.Field>>
) : Menu {
    private var scheduledFuture: ScheduledFuture<*>? = null
    private var isClosed = false
    private var message: Message? = null
    private var messageId: Long = -1
    private var currentPage = 0

    /**
     * Get the [User] of the menu
     */
    override fun getUser(): User {
        return user
    }

    /**
     * Get the message ID of the menu
     */
    override fun getMessageId(): Long {
        return messageId
    }

    /**
     * Begin the menu
     */
    override fun begin() {
        isClosed = false

        render()
        MenuManager[messageId] = this
        bumpTimeout()
    }

    /**
     * End the menu
     */
    override fun end() {
        isClosed = true

        message?.clearReactions()?.queue()
        scheduledFuture?.cancel(true)
        MenuManager.remove(messageId)
    }

    /**
     * Update/Render the menu
     */
    override fun render() {
        val embed = EmbedBuilder().run {
            setTitle(title)
            setColor(options.embedColor)

            pages[currentPage].forEach {
                addField(it)
            }

            setFooter("Page ${currentPage + 1}/${pages.size} | Requested by: ${user.asTag}")
            build()
        }

        if (message != null) {
            message!!.editMessageEmbeds(embed).queue()
        } else {
            message = channel.sendMessageEmbeds(embed).complete()
            messageId = message!!.idLong
            addPotentialEmote(options.startEmoji, options.startUnicode)
            addPotentialEmote(options.previousEmoji, options.previousUnicode)
            addPotentialEmote(options.closeEmoji, options.closeUnicode)
            addPotentialEmote(options.nextEmoji, options.nextUnicode)
            addPotentialEmote(options.endEmoji, options.endUnicode)
        }
    }

    private fun addPotentialEmote(emote: Emoji?, default: UnicodeEmoji) {
        if (emote != null) {
            message?.addReaction(emote)?.queue()
        } else {
            message?.addReaction(default)?.queue()
        }
    }

    /**
     * Handle a [MessageReaction] from the [MenuListener]
     */
    override fun handleReaction(reaction: MessageReaction) {
        if (!isClosed) {
            if (reaction.emoji.type == Emoji.Type.UNICODE) {
                when (reaction.emoji.asUnicode()) {
                    options.startUnicode -> startPage()
                    options.previousUnicode -> previousPage()
                    options.closeUnicode -> closePage()
                    options.nextUnicode -> nextPage()
                    options.endUnicode -> endPage()
                }
            }
            if (reaction.emoji.type == Emoji.Type.CUSTOM) {
                when (reaction.emoji.asCustom()) {
                    options.startEmoji -> startPage()
                    options.previousEmoji -> previousPage()
                    options.closeEmoji -> closePage()
                    options.nextEmoji -> nextPage()
                    options.endEmoji -> endPage()
                }
            }
        }
    }

    private fun startPage() {
        if (currentPage != 0) {
            currentPage = 0
            render()
        }
    }

    private fun previousPage() {
        if (currentPage - 1 >= 0) {
            currentPage--
            render()
        }
    }

    private fun closePage() {
        end()
        message?.editMessage("*Menu Closed*")?.setReplace(true)?.queue()
    }

    private fun nextPage() {
        if (currentPage + 1 < pages.size) {
            currentPage++
            render()
        }
    }

    private fun endPage() {
        if (currentPage != pages.lastIndex) {
            currentPage = pages.lastIndex
            render()
        }
    }

    /**
     * Bump the timeout, prevent from closing
     */
    override fun bumpTimeout() {
        scheduledFuture?.cancel(true)

        if (isClosed) {
            return
        }

        scheduledFuture = message?.editMessage(":x: *This menu has expired* :x:")?.setReplace(true)
                ?.queueAfter(options.timeoutMs, TimeUnit.MILLISECONDS) {
                    end()
                }
    }

    /**
     * Get if the menu is closed or not
     */
    override fun isClosed(): Boolean {
        return isClosed
    }

}

/**
 * A representation of options for a [PaginatorMenu]
 */
class PaginationOptions(
    /**
         * How many milliseconds should it take to time out the menu?
         */
        val timeoutMs: Long,

    /**
         * [nextEmoji] is used when given, instead of [nextUnicode]
         * The [CustomEmoji] that goes to the next page
         */
        val nextEmoji: CustomEmoji? = null,

    /**
         * [previousEmoji] is used when given, instead of [previousUnicode]
         * The [CustomEmoji] that goes to the previous page
         */
        val previousEmoji: CustomEmoji? = null,

    /**
         * [startEmoji] is used when given, instead of [startUnicode]
         * The [CustomEmoji] that goes to the first page
         */
        val startEmoji: CustomEmoji? = null,

    /**
         * [endEmoji] is used when given, instead of [endUnicode]
         * The [CustomEmoji] that goes to the last page
         */
        val endEmoji: CustomEmoji? = null,

    /**
         * [closeEmoji] is used when given, instead of [closeUnicode]
         * The [CustomEmoji] that closes the menu
         */
        val closeEmoji: CustomEmoji? = null,

    /**
         * [nextEmoji] is used when given, instead of [nextUnicode]
         * The Emoji that goes to the next page
         */
        val nextUnicode: UnicodeEmoji = Emoji.fromUnicode("U+25B6"),

    /**
         * [previousEmoji] is used when given, instead of [previousUnicode]
         * The Emoji that goes to the previous page
         */
        val previousUnicode: UnicodeEmoji = Emoji.fromUnicode("U+25C0"),

    /**
         * [startEmoji] is used when given, instead of [startUnicode]
         * The Emoji that goes to the first page
         */
        val startUnicode: UnicodeEmoji = Emoji.fromUnicode("U+23EE"),

    /**
         * [endEmoji] is used when given, instead of [endUnicode]
         * The Emoji that goes to the last page
         */
        val endUnicode: UnicodeEmoji = Emoji.fromUnicode("U+23ED"),

    /**
         * [closeEmoji] is used when given, instead of [closeUnicode]
         * The Emoji that closes the menu
         */
        val closeUnicode: UnicodeEmoji = Emoji.fromUnicode("U+274C"),

    /**
         * The color of the [MessageEmbed]
         */
        val embedColor: Int = 0x000000,
)
