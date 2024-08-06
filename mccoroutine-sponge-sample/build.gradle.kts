import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version ("8.1.1")
}

publishing {
    publications {
        (findByName("mavenJava") as MavenPublication).artifact(tasks.findByName("shadowJar")!!)
    }
}

tasks.withType<ShadowJar> {
    dependsOn("jar")
    archiveClassifier.set("shadowJar")

    // Change the output folder of the plugin.
    //  destinationDir = File("C:\\temp\\Sponge\\Sponge-2825-7.1.6\\mods")
}

repositories {
    maven {
        url = uri("https://repo.spongepowered.org/maven")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
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
    testImplementation("org.spongepowered:spongeapi:7.2.0")
}
