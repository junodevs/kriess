/*
 * Copyright 2017-2020 Avery Clifton and Logan Devecka
 *      Taken from https://github.com/sandrabot/sandra/
 * Copyright 2021 Juno Developers
 *      Modified to work in the context of Kriess
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.junodevs.discord.kriess.command.arguments

import net.dv8tion.jda.api.entities.*
import tech.junodevs.discord.kriess.command.Command
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

/**
 * Wrapper class for casting argument parsing results.
 * Use the method with the corresponding name to the desired type.
 * If no name is given, the type name will be assumed.
 * If the argument was not parsed all methods will return null.
 */
class ArgumentResult(val results: Map<String, Any>) {

    /**
     * Additional way to check if an argument was parsed other than the elvis operator.
     */
    operator fun contains(name: String): Boolean = name in results

    /**
     * Returns the argument with the [name] casted as [T].
     * If the wrong type is provided a casting exception will be thrown.
     * If the argument was not parsed null will be returned.
     */
    inline fun <reified T> get(name: String): T? = if (name in results) results[name] as T else null

    /**
     * Returns the array of type [T] with the name [name].
     * Note that despite the name, a [List] is returned.
     */
    inline fun <reified T> array(name: String): List<T>? = if (name in results) {
        (results[name] as List<*>).filterIsInstance<T>()
    } else null

    /* JDA Objects */

    fun channel(name: String = "channel"): TextChannel? = get(name)
    fun emote(name: String = "emote"): Emote? = get(name)
    fun role(name: String = "role"): Role? = get(name)
    fun user(name: String = "user"): User? = get(name)
    fun voice(name: String = "voice"): VoiceChannel? = get(name)

    /* Kriess Objects */

    fun command(name: String = "command"): Command? = get(name)

    /* Native Objects */

    fun digit(name: String = "digit"): Long? = get(name)
    fun text(name: String = "text"): String? = get(name)
    fun word(name: String = "word"): String? = get(name)

    @ExperimentalTime
    fun duration(name: String = "duration"): Duration? =
        get<Long>(name)?.toDuration(TimeUnit.SECONDS)

    /**
     * Flags do not have a default name.
     */
    fun flag(name: String): Boolean? = get(name)

}