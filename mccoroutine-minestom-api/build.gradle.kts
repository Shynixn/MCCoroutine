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
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
    compileOnly("net.minestom:minestom-snapshots:7320437640")
    implementation("dev.hollowcube:minestom-ce-extensions:1.2.0")
    testImplementation("net.minestom:minestom-snapshots:7320437640")
}
