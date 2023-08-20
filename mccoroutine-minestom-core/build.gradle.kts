repositories {
    maven {
        url = uri("https://jitpack.io")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(project(":mccoroutine-minestom-api"))

    compileOnly("net.kyori:adventure-text-logger-slf4j:4.12.0")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    compileOnly("com.github.Minestom:Minestom:c5047b8037")
    testImplementation("com.github.Minestom:Minestom:c5047b8037")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
}
