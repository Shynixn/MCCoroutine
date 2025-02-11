import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    maven {
        url = uri("https://jitpack.io")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "21"
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    compileOnly("net.minestom:minestom-snapshots:6fc64e3a5d")
    compileOnly("dev.hollowcube:minestom-ce-extensions:1.2.0")
    testImplementation("net.minestom:minestom-snapshots:6fc64e3a5d")
}
