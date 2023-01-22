repositories {
    maven {
        url = uri("https://maven.fabricmc.net")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(project(":mccoroutine-fabric-api"))
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    compileOnly("net.fabricmc:fabric-loader:0.14.13")
    compileOnly("net.fabricmc.fabric-api:fabric-api:0.73.0+1.19.3")
    testImplementation("net.fabricmc.fabric-api:fabric-api:0.73.0+1.19.3")
}
