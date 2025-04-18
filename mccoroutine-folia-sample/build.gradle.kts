import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.github.johnrengelman.shadow") version ("8.1.1")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
    }
}

tasks.withType<ShadowJar> {
    dependsOn("jar")
    archiveClassifier.set("shadowJar")

    // Change the output folder of the plugin.
    // destinationDirectory.set(File("C:\\temp\\Folia\\plugins"))
}

repositories {
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    implementation(project(":mccoroutine-folia-api"))
    implementation(project(":mccoroutine-folia-core"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.9")

    compileOnly("dev.folia:folia-api:1.21.4-R0.1-SNAPSHOT")

    testImplementation("dev.folia:folia-api:1.21.4-R0.1-SNAPSHOT")
}
