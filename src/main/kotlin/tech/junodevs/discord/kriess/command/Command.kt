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

package tech.junodevs.discord.kriess.command

import tech.junodevs.discord.kriess.command.arguments.Argument
import tech.junodevs.discord.kriess.utils.removeExtraSpaces
import tech.junodevs.discord.kriess.utils.splitSpaces
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.isSubclassOf

/**
 * A representation of a Kriess Command
 */
abstract class Command(
        /**
         * The [name] of this [Command]
         */
        val name: String,

        /**
         * The [aliases] that also resolve to this [Command]
         */
        val aliases: List<String> = listOf(),

        /**
         * The textual representation of [arguments] that gets passed to [Argument.compile] and put in [Command.arguments]
         */
        arguments: String = "",

        /**
         * The [description] of the command
         */
        val description: String,

        /**
         * The [CommandCategory] that this [Command] is a part of
         */
        val category: CommandCategory,

        /**
         * Should this [Command] be shown in help?
         */
        val showInHelp: Boolean = true,

        /**
         * Should this [Command] be owner only?
         */
        val ownerOnly: Boolean = false,
) {

    /**
     * The [Command]s that are defined as sub classes of this one
     */
    val children: List<Command> = this::class.nestedClasses.filter {
        it.isSubclassOf(Command::class)
    }.map {
        (it.createInstance() as Command).also { child -> child.parent = this }
    }.toList()

    /**
     * Only set if this [Command] is a Sub-Command
     */
    var parent: Command? = null
        internal set

    /**
     * Is this [Command] a Sub-Command?
     */
    val isSubcommand: Boolean
        get() = parent != null

    /**
     * The [Argument]s to be taken into a [CommandEvent]
     */
    val arguments: List<Argument> = Argument.compile(arguments)

    /**
     * How does one use this [Command]?
     */
    val usage: String by lazy {
        if (this.arguments.isEmpty()) path.replace(":", " ") else {
            path.replace(":", " ") + " " + this.arguments.joinToString(" ") { it.usage }
        }
    }

    /**
     * Where exactly is this [Command] located in the tree?
     */
    val path: String by lazy {
        var currentCommand = this
        val builder = StringBuilder()
        do {
            builder.insert(0, currentCommand.name + ":")
            currentCommand = currentCommand.parent ?: break
        } while (true)
        builder.substring(0, builder.lastIndex)
    }

    /**
     * Should the [event] be allowed to continue to the [Command.handle]?
     * This is triggered before the [Command.handle], and if it returns true, will continue
     */
    open fun preHandle(event: CommandEvent): Boolean {
        return true
    }

    /**
     * Handle the [event] passed by a [tech.junodevs.discord.kriess.managers.ICommandManager]
     */
    abstract fun handle(event: CommandEvent)

    /**
     * Finds children of this command by recursively walking the command tree.
     * The returned pair is the possible child and the remaining arguments.
     * If no child was found the command will be null.
     * Otherwise, it is guaranteed that the remaining arguments have changed.
     */
    fun findChild(args: String): Pair<Command?, String> {
        // Attempt to find a child with the first word as the alias
        val firstArg = args.splitSpaces().first()
        val child = children.firstOrNull {
            arrayOf(it.name, *it.aliases.toTypedArray()).any { alias ->
                firstArg.equals(alias, ignoreCase = true)
            }
        }
        var arguments = args
        // Use recursion to continue walking the command tree
        val nestedCommand = if (child != null) {
            arguments = arguments.removePrefix(firstArg).removeExtraSpaces()
            // If there was only one word there's nothing else to find
            if (arguments.isNotEmpty()) {
                val recursive = child.findChild(arguments)
                // Reassign the arguments if a command was found and a word was used
                if (recursive.first != null) {
                    arguments = recursive.second
                    recursive.first
                } else child
            } else child
        } else child

        return nestedCommand to arguments
    }
}
