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
        url = uri("https://repo.spongepowered.org/maven")
    }
}

dependencies {
    implementation(project(":mccoroutine-sponge-api"))

    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")

    testCompile("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")

    compileOnly("org.spongepowered:spongeapi:7.2.0")
    testCompile("org.spongepowered:spongeapi:7.2.0")
}
