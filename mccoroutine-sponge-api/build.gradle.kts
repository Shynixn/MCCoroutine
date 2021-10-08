repositories {
    maven {
        url = uri("https://repo.spongepowered.org/maven")
    }
}

dependencies {
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    compileOnly("org.spongepowered:spongeapi:7.2.0")
    testCompile("org.spongepowered:spongeapi:7.2.0")
}
