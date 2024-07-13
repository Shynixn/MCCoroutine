repositories {
    maven {
        url = uri("https://nexus.velocitypowered.com/repository/maven-public")
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
    compileOnly("com.velocitypowered:velocity-api:3.0.1")
    testImplementation("com.velocitypowered:velocity-api:3.0.1")
}
