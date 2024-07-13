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

    compileOnly("net.kyori:adventure-text-logger-slf4j:4.12.0")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
    implementation("net.minestom:minestom-snapshots:7320437640")
    implementation("dev.hollowcube:minestom-ce-extensions:1.2.0")
    testImplementation("net.minestom:minestom-snapshots:7320437640")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
}
