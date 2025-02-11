import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    maven {
        url = uri("https://nexus.velocitypowered.com/repository/maven-public")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(project(":mccoroutine-velocity-api"))

    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    compileOnly("org.apache.logging.log4j:log4j-core:2.17.2")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    testImplementation(files("lib/velocity.jar"))
    testImplementation("com.velocitypowered:velocity-api:3.1.1")
}
