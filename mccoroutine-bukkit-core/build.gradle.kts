import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

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
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
}

dependencies {
    implementation(project(":mccoroutine-bukkit-api"))

    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    compileOnly("org.spigotmc:spigot-api:1.16.3-R0.1-SNAPSHOT")

    testCompile("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    testCompile("org.spigotmc:spigot-api:1.16.3-R0.1-SNAPSHOT")
}
