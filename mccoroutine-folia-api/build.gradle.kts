repositories {
    maven {
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    compileOnly("dev.folia:folia-api:1.20.1-R0.1-20230615.235213-1")
}
