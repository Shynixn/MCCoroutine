repositories {
    maven {
        url = uri("https://jitpack.io")
        metadataSources {
            artifact()
        }
    }
}

dependencies {
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    compileOnly("com.github.Minestom.Minestom:Minestom:-84846f663b-1")
    testCompile("com.github.Minestom.Minestom:Minestom:-84846f663b-1")
}
