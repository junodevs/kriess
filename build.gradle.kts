import org.jetbrains.kotlin.backend.wasm.lower.excludeDeclarationsFromCodegen
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    `java-library`
    `maven-publish`

    kotlin("jvm") version "1.4.10"
    id("com.github.johnrengelman.shadow") version "6.0.0"
}

group = "tech.junodevs.discord"
version = "0.2.0"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    listOf("stdlib-jdk8", "reflect").forEach { implementation(kotlin(it)) }

    // JDA
    api("net.dv8tion:JDA:4.2.0_207")

    // Logger
    implementation("ch.qos.logback:logback-classic:1.2.3")

    // Internal Utilities
    implementation("org.yaml:snakeyaml:1.27")
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
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                artifact(tasks["sourcesJar"])
            }
        }
    }
}
