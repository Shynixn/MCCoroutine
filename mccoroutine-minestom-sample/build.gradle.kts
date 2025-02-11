import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.github.johnrengelman.shadow") version ("8.1.1")
}

repositories {
    maven {
        url = uri("https://jitpack.io")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "21"
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(project(":mccoroutine-minestom-api"))
    implementation(project(":mccoroutine-minestom-core"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.9")

    compileOnly("dev.hollowcube:minestom-ce-extensions:1.2.0")
    implementation("net.minestom:minestom-snapshots:6fc64e3a5d")
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "com.github.shynixn.mccoroutine.minestom.sample.server.MCoroutineSampleServerKt"
    }
}

tasks.withType<ShadowJar> {
    dependsOn("jar")
    archiveClassifier.set("shadowJar")

    // Change the output folder of the plugin.
    // destinationDirectory.set(File("C:\\temp\\minestom"))
}
