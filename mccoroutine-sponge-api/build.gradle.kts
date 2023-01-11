repositories {
    maven {
        url = uri("https://repo.spongepowered.org/maven")
    }
}

dependencies {
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    compileOnly("org.spongepowered:spongeapi:7.2.0")
    testImplementation("org.spongepowered:spongeapi:7.2.0")
}
