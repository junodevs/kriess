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

import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent
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
        if (event is GuildMessageReactionAddEvent) {
            if (event.user.isBot) return

            val history = try {
                event.channel.getHistoryAround(event.messageId, 1).complete() ?: return
            } catch (ex: Exception) {
                // If we can't get history, ignore it.
                return
            }

            val msg = history.getMessageById(event.messageId) ?: return
            if (msg.author != event.jda.selfUser) return

            val menu = MenuManager[msg.idLong]

            if (menu != null) {
                if (menu.getUser() == event.user) {
                    menu.handleReaction(event.reaction)
                    menu.bumpTimeout()
                    return
                }
                if (event.reactionEmote.isEmote) {
                    msg.removeReaction(event.reactionEmote.emote, event.user).queue()
                } else {
                    msg.removeReaction(event.reactionEmote.emoji, event.user).queue()
                }
            }
        } else if (event is GuildMessageReactionRemoveEvent) {
            val user = event.user ?: return // we need the user >.>
            if (user.isBot) return

            val history = try {
                event.channel.getHistoryAround(event.messageId, 1).complete() ?: return
            } catch (ex: Exception) {
                // If we can't get history, ignore it.
                return
            }

            val msg = history.getMessageById(event.messageId) ?: return
            if (msg.author != event.jda.selfUser) return

            val menu = MenuManager[msg.idLong]

            if (menu != null) {
                if (menu.getUser() == event.user) {
                    menu.handleReaction(event.reaction)
                    menu.bumpTimeout()
                    return
                }
                // On an Add we remove the reaction, but here we do nothing because the reaction is already gone
            }
        }
    }


}