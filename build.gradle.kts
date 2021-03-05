import org.apache.commons.io.output.ByteArrayOutputStream
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    `java-library`
    `maven-publish`

    kotlin("jvm") version "1.4.10"
    id("com.github.gmazzo.buildconfig") version "2.0.2"
    id("com.github.johnrengelman.shadow") version "6.0.0"
}

group = "tech.junodevs.discord"
version = "${rootProject.property("major")}.${rootProject.property("minor")}.${rootProject.property("patch")}"

val commit = runCommand(arrayListOf("git", "rev-parse", "HEAD"))

buildConfig {
    packageName = "tech.junodevs.discord.kriess"
    className = "KriessInfo"
    buildConfigField("String", "VERSION", "\"${version}\"")
    buildConfigField("String", "COMMIT", "\"$commit\"")
    buildConfigField("long", "BUILD_TIME", "${System.currentTimeMillis()}L")
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    listOf("stdlib-jdk8", "reflect").forEach { implementation(kotlin(it)) }

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")

    // JDA
    api("net.dv8tion:JDA:4.2.0_229")

    // Logger
    api("ch.qos.logback:logback-classic:1.2.3")

    // Utilities
    api("org.yaml:snakeyaml:1.27")
    api("me.xdrop:fuzzywuzzy:1.3.1")

    // Internal Utilities
    implementation("com.google.guava:guava:28.0-jre")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "1.8"

val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets.main.get().allSource)
}

publishing {
    repositories {
        maven {
            name = "internal.repo"
            url = uri("$path/../../maven-repo")
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
        }
    }
}

fun runCommand(commands: List<String>): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine = commands
        standardOutput = stdout
    }
    return stdout.toString("utf-8").trim()
}