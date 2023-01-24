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

import net.dv8tion.jda.api.entities.MessageReaction
import net.dv8tion.jda.api.entities.User

/**
 * A simple Menu interface, gets the ball rolling
 */
interface Menu {

    /**
     * Begin the menu
     */
    fun begin()

    /**
     * End the menu
     */
    fun end()

    /**
     * Update/Render the menu
     */
    fun render()

    /**
     * Handle a [MessageReaction] from the [MenuListener]
     */
    fun handleReaction(reaction: MessageReaction)

    /**
     * Bump the timeout, prevent from closing
     */
    fun bumpTimeout()

    /**
     * Get the [User] of the menu
     */
    fun getUser(): User

    /**
     * Get the message ID of the menu
     */
    fun getMessageId(): Long

    /**
     * Get if the menu is closed or not
     */
    fun isClosed(): Boolean

}