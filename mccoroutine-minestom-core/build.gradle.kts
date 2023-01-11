repositories {
    maven {
        url = uri("https://jitpack.io")
        metadataSources {
            artifact()
        }
    }
}

dependencies {
    implementation(project(":mccoroutine-minestom-api"))

    compileOnly("net.kyori:adventure-text-logger-slf4j:4.12.0")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    compileOnly("com.github.Minestom.Minestom:Minestom:-84846f663b-1")
    testImplementation("com.github.Minestom.Minestom:Minestom:-84846f663b-1")
}
