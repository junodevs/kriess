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

class PaginatorMenu(
    private val channel: TextChannel,
    private val user: User,
    private val title: String,
    private val options: PaginationOptions,
    private val pages: List<List<MessageEmbed.Field>>
) : Menu {
    private var scheduledFuture: ScheduledFuture<*>? = null
    private var isClosed = false
    private var message: Message? = null
    private var messageId: Long = -1
    private var currentPage = 0

    override fun getUser(): User {
        return user
    }

    override fun getMessageId(): Long {
        return messageId
    }

    override fun begin() {
        isClosed = false

        render()
        MenuManager[messageId] = this
        bumpTimeout()
    }

    override fun end() {
        isClosed = true

        message?.clearReactions()?.queue()
        scheduledFuture?.cancel(true)
        MenuManager.remove(messageId)
    }

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
            message!!.editMessage(embed).queue()
        } else {
            message = channel.sendMessage(embed).complete()
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

    override fun isClosed(): Boolean {
        return isClosed
    }

}

class PaginationOptions(
    val timeoutMs: Long,
    val nextEmote: Emote? = null,
    val previousEmote: Emote? = null,
    val startEmote: Emote? = null,
    val endEmote: Emote? = null,
    val closeEmote: Emote? = null,
    val nextEmoji: String = "\u25b6\ufe0f",
    val previousEmoji: String = "\u25c0\ufe0f",
    val startEmoji: String = "\u23ee\ufe0f",
    val endEmoji: String = "\u23ed\ufe0f",
    val closeEmoji: String = "\u274c",
    val embedColor: Int = 0x000000,
)
