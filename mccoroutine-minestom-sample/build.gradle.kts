import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version ("8.1.1")
}

repositories {
    maven {
        url = uri("https://jitpack.io")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation(project(":mccoroutine-minestom-api"))
    implementation(project(":mccoroutine-minestom-core"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.9.0-RC")

    implementation("dev.hollowcube:minestom-ce-extensions:1.2.0")
    implementation("net.minestom:minestom-snapshots:7320437640")
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "com.github.shynixn.mccoroutine.minestom.sample.MCoroutineSampleServerKt"
    }
}

tasks.withType<ShadowJar> {
    dependsOn("jar")
    archiveClassifier.set("shadowJar")
    archiveFileName.set("${archiveBaseName.get()}-${archiveVersion.get()}.${archiveExtension.get()}")

    // Change the output folder of the plugin.
    // destinationDir = File("..\\extensions")
}
