import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version ("8.1.1")
}

publishing {
    publications {
        (findByName("mavenJava") as MavenPublication).artifact(tasks.findByName("shadowJar")!!)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<ShadowJar> {
    dependsOn("jar")
    archiveClassifier.set("shadowJar")
    archiveFileName.set("${archiveBaseName.get()}-${archiveVersion.get()}.${archiveExtension.get()}")

    // Change the output folder of the plugin.
    // destinationDir = File("C:\\temp\\Folia\\plugins")
}

repositories {
    maven {
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
}

dependencies {
    implementation(project(":mccoroutine-folia-api"))
    implementation(project(":mccoroutine-folia-core"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.9.0-RC")

    compileOnly("dev.folia:folia-api:1.20.1-R0.1-20230615.235213-1")

    testImplementation("org.spigotmc:spigot-api:1.16.3-R0.1-SNAPSHOT")
}
