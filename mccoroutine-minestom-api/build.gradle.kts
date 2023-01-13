repositories {
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    components {
        all {
            allVariants {
                attributes {
                    attribute(Attribute.of("org.gradle.jvm.version", Int::class.javaObjectType), 8)
                }
            }
        }
    }

    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    compileOnly("com.github.Minestom:Minestom:8eb089bf3e")
    testImplementation("com.github.Minestom:Minestom:8eb089bf3e")
}
