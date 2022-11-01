import org.apache.commons.io.output.ByteArrayOutputStream
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    `java-library`
    `maven-publish`

    kotlin("jvm") version "1.7.20"
    id("org.jetbrains.dokka") version "1.7.20"
    id("com.github.gmazzo.buildconfig") version "3.1.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "tech.junodevs.discord"
version = "${rootProject.property("major")}.${rootProject.property("minor")}.${rootProject.property("patch")}"

val commit = runCommand(arrayListOf("git", "rev-parse", "HEAD"))

buildConfig {
    packageName("tech.junodevs.discord.kriess")
    className("KriessInfo")
    buildConfigField("String", "VERSION", "\"${version}\"")
    buildConfigField("String", "COMMIT", "\"$commit\"")
    buildConfigField("long", "BUILD_TIME", "${System.currentTimeMillis()}L")
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven") {
        content {
            includeGroup("org.jetbrains.kotlinx")
        }
    }

    maven("https://m2.dv8tion.net/releases") {
        name = "m2-dv8tion"
    }
}

dependencies {
    listOf("stdlib-jdk8", "reflect").forEach { implementation(kotlin(it)) }

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    // JDA
    api("net.dv8tion:JDA:4.4.0_350")

    // Logger
    api("ch.qos.logback:logback-classic:1.4.4")

    // Utilities
    api("org.yaml:snakeyaml:1.33")
    api("me.xdrop:fuzzywuzzy:1.4.0")

    // Internal Utilities
    implementation("com.google.guava:guava:31.1-jre")

    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.7.20")
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