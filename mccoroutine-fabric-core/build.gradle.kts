import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    maven {
        url = uri("https://maven.fabricmc.net")
    }
    maven {
        url = uri("https://libraries.minecraft.net") // Brigadier
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
    implementation(project(":mccoroutine-fabric-api"))
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    compileOnly("net.fabricmc:fabric-loader:0.14.13")
    compileOnly("net.fabricmc.fabric-api:fabric-api:0.73.0+1.19.3")
    testImplementation("com.mojang:brigadier:1.0.18")
    testImplementation("net.fabricmc:fabric-loader:0.14.13")
    testImplementation("net.fabricmc.fabric-api:fabric-api:0.73.0+1.19.3")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
}
