import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.github.johnrengelman.shadow") version ("8.1.1")
    id("org.jetbrains.kotlin.kapt") // Required to generate the velocity-plugin.json file.
}

repositories {
    maven {
        url = uri("https://nexus.velocitypowered.com/repository/maven-public")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.shadowJar {
    dependsOn("jar")
    archiveClassifier.set("shadowJar")

    // Change the output folder of the plugin.
    // destinationDirectory = file("C:\\temp\\Velocity\\plugins")
}

dependencies {
    implementation(project(":mccoroutine-velocity-api"))
    implementation(project(":mccoroutine-velocity-core"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.9")

    compileOnly("com.velocitypowered:velocity-api:3.0.1")
    kapt("com.velocitypowered:velocity-api:3.0.1")
    testImplementation("com.velocitypowered:velocity-api:3.0.1")
}
