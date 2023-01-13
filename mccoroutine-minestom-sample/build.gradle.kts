import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version ("2.0.4")
}

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

    implementation(project(":mccoroutine-minestom-api"))
    implementation(project(":mccoroutine-minestom-core"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.9")

    implementation("com.github.Minestom:Minestom:8eb089bf3e")
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "com.github.shynixn.mccoroutine.minestom.sample.MCoroutineSampleServerKt"
    }
}

tasks.withType<ShadowJar> {
    dependsOn("jar")
    classifier = "shadowJar"
    archiveName = "$baseName.$extension"

    // Change the output folder of the plugin.
    // destinationDir = File("..\\extensions")
}
