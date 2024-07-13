repositories {
    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.16.3-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
}
