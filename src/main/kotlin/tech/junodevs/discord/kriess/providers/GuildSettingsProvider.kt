package tech.junodevs.discord.kriess.providers

interface GuildSettingsProvider {

    val realPrefix: String

    fun toMap(): Map<String, Any?>

}