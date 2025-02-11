import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.github.johnrengelman.shadow") version ("8.1.1")
}

tasks.withType<ShadowJar> {
    dependsOn("jar")
    archiveClassifier.set("shadowJar")
    // Change the output folder of the plugin.
    // destinationDir = File("C:\\temp\\plugins\\")
}

repositories {
    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(project(":mccoroutine-bukkit-api"))
    implementation(project(":mccoroutine-bukkit-core"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.9")

    compileOnly("org.spigotmc:spigot-api:1.16.3-R0.1-SNAPSHOT")

    testImplementation(project(":mccoroutine-bukkit-test"))
    testImplementation("org.spigotmc:spigot-api:1.16.3-R0.1-SNAPSHOT")
}
