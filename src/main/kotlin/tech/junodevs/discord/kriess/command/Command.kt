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

import tech.junodevs.discord.kriess.command.arguments.Argument

abstract class Command(
    val name: String,
    val aliases: List<String> = listOf(),
    arguments: String = "",
    val description: String,
    val category: CommandCategory,
    val showInHelp: Boolean = true,
    val ownerOnly: Boolean = false,
) {

    val arguments: List<Argument> = Argument.compile(arguments)
    val usage: String by lazy {
        if (this.arguments.isEmpty()) name else {
            this.arguments.joinToString("") { it.usage }
        }
    }

    open fun preHandle(event: CommandEvent): Boolean {
        return true
    }

    abstract fun handle(event: CommandEvent)

}
