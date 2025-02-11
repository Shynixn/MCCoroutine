import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
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
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    compileOnly("net.md-5:bungeecord-api:1.16-R0.5-SNAPSHOT")
    testImplementation("net.md-5:bungeecord-api:1.16-R0.5-SNAPSHOT")
}
