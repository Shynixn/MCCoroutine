import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.net.URL
import java.nio.file.Files
import java.util.*

plugins {
    id("com.github.johnrengelman.shadow") version ("2.0.4")
}

publishing {
    publications {
        (findByName("mavenJava") as MavenPublication).artifact(tasks.findByName("shadowJar")!!)
    }
}

tasks.withType<ShadowJar> {
    dependsOn("jar")
    archiveName = "$baseName-$version.$extension"
}

repositories {
    maven {
        url = uri("https://repo.spongepowered.org/maven")
    }
}

tasks.register("customDependencies") {
    if (!buildDir.exists()) {
        buildDir.mkdir()
    }

    val file = File(buildDir, "SpongeCommon.jar")

    if (!file.exists()) {
        URL("https://github.com/SpongePowered/Sponge/releases/download/v7.2.0/spongecommon-1.12.2-7.2.0.jar").openStream()
            .use {
                Files.copy(it, file.toPath())
            }
    }
}


dependencies {
    implementation(project(":mccoroutine-sponge-api"))

    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")

    testCompile("org.apache.logging.log4j:log4j-api:2.17.2")
    testCompile("it.unimi.dsi:fastutil:7.0.13")
    testCompile("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    testCompile(files("build/SpongeCommon.jar"))

    compileOnly("org.spongepowered:spongeapi:7.2.0")
    testCompile("org.spongepowered:spongeapi:7.2.0")
}
