package tech.junodevs.discord.kriess.command

private var nextPriority = 0

class CommandCategory(val name: String, val priority: Int = nextPriority++)