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

/**
 * Keep track of all of the open menus
 */
object MenuManager {

    private val menus: MutableMap<Long, Menu> = mutableMapOf()

    /**
     * A list of the open [Menu]s
     */
    val currentMenus: List<Menu>
        get() = menus.values.filter { !it.isClosed() }

    /**
     * Get a [Menu] given the [message] id
     */
    operator fun get(message: Long) = menus[message]

    /**
     * Set a [Menu] to a [message] id
     */
    operator fun set(message: Long, menu: Menu) = menus.put(message, menu)

    /**
     * Remove a [Menu] by [message] id
     */
    fun remove(message: Long) {
        menus.remove(message)
    }
}