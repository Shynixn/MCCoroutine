repositories {
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(project(":mccoroutine-folia-api"))

    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    compileOnly("dev.folia:folia-api:1.20.1-R0.1-20230615.235213-1")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    testImplementation("dev.folia:folia-api:1.20.1-R0.1-20230615.235213-1")
}
