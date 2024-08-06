repositories {
    maven {
        url = uri("https://repo.spongepowered.org/maven")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    implementation(project(":mccoroutine-sponge-api"))

    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")

    testImplementation("org.apache.logging.log4j:log4j-api:2.17.2")
    testImplementation("it.unimi.dsi:fastutil:7.0.13")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    testImplementation(files("lib/SpongeCommon.jar"))

    compileOnly("org.spongepowered:spongeapi:7.2.0")
    testImplementation("org.spongepowered:spongeapi:7.2.0")
}
