import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    maven {
        url = uri("https://nexus.velocitypowered.com/repository/maven-public")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(project(":mccoroutine-velocity-api"))

    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
    compileOnly("com.velocitypowered:velocity-api:3.0.1")
    compileOnly("org.apache.logging.log4j:log4j-core:2.17.2")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
    testImplementation(files("lib/velocity.jar"))
    testImplementation("com.velocitypowered:velocity-api:3.0.1")
}
