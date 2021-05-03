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

package tech.junodevs.discord.kriess.exceptions

import tech.junodevs.discord.kriess.command.CommandEvent
import tech.junodevs.discord.kriess.command.arguments.Argument

/**
 * An exception thrown when the parsing of an [Argument] is required, but not found
 */
class MissingArgumentException(val args: String, val argument: Argument) : RuntimeException(
    "Required argument ${argument.name} is missing in text $args"
)