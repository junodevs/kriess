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

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent
import net.dv8tion.jda.api.hooks.EventListener

/**
 * Listens for reactions on menus, passing them along and removing the reaction if the user that reacted was not the one
 * using the menu.
 */
object MenuListener : EventListener {

    /**
     * Called by JDA when any event occurs
     * These events are filtered for just the reaction events
     */
    override fun onEvent(event: GenericEvent) {
        when (event) {
            is MessageReactionAddEvent -> {
                val menuCheck = checkMenu(event)
                if (menuCheck.first) {
                    menuCheck.second!!.removeReaction(event.reaction.emoji, event.user!!).queue()
                }
            }

            is MessageReactionRemoveEvent -> {
                checkMenu(event)
            }
        }
    }

    /**
     * Check the GenericMessageReactionEvent, verify that it is a menu, and whether we should respond.
     * The pair returned is in the following format <Menu Valid, Menu Message>
     */
    private fun checkMenu(event: GenericMessageReactionEvent): Pair<Boolean, Message?> {
        val user = event.retrieveUser().complete()
        if (user.isBot) return Pair(false, null)

        val history = try {
            event.channel.getHistoryAround(event.messageId, 1).complete() ?: return Pair(false, null)
        } catch (ex: Exception) {
            // If we can't get history, ignore it.
            return Pair(false, null)
        }

        val msg = history.getMessageById(event.messageId) ?: return Pair(false, null)
        if (msg.author != event.jda.selfUser) return Pair(false, null)

        val menu = MenuManager[msg.idLong]

        if (menu != null) {
            if (menu.getUser() == event.user) {
                menu.handleReaction(event.reaction)
                menu.bumpTimeout()
            }
            return Pair(true, msg)
        }
        return Pair(false, null)
    }

}