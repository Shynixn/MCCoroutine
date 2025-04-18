import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    // Everything works during runtime. For Java < 17 Bukkit mode is used. Otherwise Folia if available.
    components {
        all {
            allVariants {
                attributes {
                    attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 8)
                }
            }
        }
    }

    implementation(project(":mccoroutine-folia-api"))

    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    compileOnly("dev.folia:folia-api:1.21.4-R0.1-SNAPSHOT")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    testImplementation("dev.folia:folia-api:1.21.4-R0.1-SNAPSHOT")
}
