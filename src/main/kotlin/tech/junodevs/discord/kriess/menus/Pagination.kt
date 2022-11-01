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

package tech.junodevs.discord.kriess.menus

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.*
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
            addPotentialEmote(options.startEmote, options.startEmoji)
            addPotentialEmote(options.previousEmote, options.previousEmoji)
            addPotentialEmote(options.closeEmote, options.closeEmoji)
            addPotentialEmote(options.nextEmote, options.nextEmoji)
            addPotentialEmote(options.endEmote, options.endEmoji)
        }
    }

    private fun addPotentialEmote(emote: Emote?, default: String) {
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
            if (reaction.reactionEmote.isEmoji) {
                when (reaction.reactionEmote.emoji) {
                    options.startEmoji -> startPage()
                    options.previousEmoji -> previousPage()
                    options.closeEmoji -> closePage()
                    options.nextEmoji -> nextPage()
                    options.endEmoji -> endPage()
                }
            }
            if (reaction.reactionEmote.isEmote) {
                when (reaction.reactionEmote.emote) {
                    options.startEmote -> startPage()
                    options.previousEmote -> previousPage()
                    options.closeEmote -> closePage()
                    options.nextEmote -> nextPage()
                    options.endEmote -> endPage()
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
        message?.editMessage("*Menu Closed*")?.override(true)?.queue()
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

        scheduledFuture = message?.editMessage(":x: *This menu has expired* :x:")?.override(true)
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
         * [nextEmote] is used when given, instead of [nextEmoji]
         * The [Emote] that goes to the next page
         */
        val nextEmote: Emote? = null,

        /**
         * [previousEmote] is used when given, instead of [previousEmoji]
         * The [Emote] that goes to the previous page
         */
        val previousEmote: Emote? = null,

        /**
         * [startEmote] is used when given, instead of [startEmoji]
         * The [Emote] that goes to the first page
         */
        val startEmote: Emote? = null,

        /**
         * [endEmote] is used when given, instead of [endEmoji]
         * The [Emote] that goes to the last page
         */
        val endEmote: Emote? = null,

        /**
         * [closeEmote] is used when given, instead of [closeEmoji]
         * The [Emote] that closes the menu
         */
        val closeEmote: Emote? = null,

        /**
         * [nextEmote] is used when given, instead of [nextEmoji]
         * The Emoji that goes to the next page
         */
        val nextEmoji: String = "\u25b6\ufe0f",

        /**
         * [previousEmote] is used when given, instead of [previousEmoji]
         * The Emoji that goes to the previous page
         */
        val previousEmoji: String = "\u25c0\ufe0f",

        /**
         * [startEmote] is used when given, instead of [startEmoji]
         * The Emoji that goes to the first page
         */
        val startEmoji: String = "\u23ee\ufe0f",

        /**
         * [endEmote] is used when given, instead of [endEmoji]
         * The Emoji that goes to the last page
         */
        val endEmoji: String = "\u23ed\ufe0f",

        /**
         * [closeEmote] is used when given, instead of [closeEmoji]
         * The Emoji that closes the menu
         */
        val closeEmoji: String = "\u274c",

        /**
         * The color of the [MessageEmbed]
         */
        val embedColor: Int = 0x000000,
)
