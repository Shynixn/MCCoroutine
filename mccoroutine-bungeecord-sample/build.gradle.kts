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
    // destinationDir = File("C:\\temp\\BungeeCord\\plugins")
}

repositories {
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
}

dependencies {
    implementation(project(":mccoroutine-bungeecord-api"))
    implementation(project(":mccoroutine-bungeecord-core"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.9")

    compileOnly("net.md-5:bungeecord-api:1.16-R0.5-SNAPSHOT")
    testCompile("net.md-5:bungeecord-api:1.16-R0.5-SNAPSHOT")
}
