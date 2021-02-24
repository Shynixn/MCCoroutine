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

    // Change the output folder of the plugin.
    // destinationDir = File("D:\\Benutzer\\Temp\\SpongePowered\\Sponge-2825-7.1.6\\mods")
}

repositories {
    maven {
        url = uri("https://repo.spongepowered.org/maven")
    }
}

dependencies {
    implementation(project(":mccoroutine-sponge-api"))
    implementation(project(":mccoroutine-sponge-core"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.2.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.2.2")

    compileOnly("com.google.guava:guava:23.0")
    compileOnly("org.spongepowered:spongeapi:7.2.0")
    testCompile("org.spongepowered:spongeapi:7.2.0")
}
