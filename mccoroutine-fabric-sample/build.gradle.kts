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
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    compileOnly("com.github.Minestom:Minestom:8eb089bf3e")
    testImplementation("com.github.Minestom:Minestom:8eb089bf3e")
}
