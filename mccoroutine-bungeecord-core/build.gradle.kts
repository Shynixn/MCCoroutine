repositories {
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
}

dependencies {
    implementation(project(":mccoroutine-bungeecord-api"))

    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.9")

    compileOnly("net.md-5:bungeecord-api:1.16-R0.5-SNAPSHOT")
    testCompile("net.md-5:bungeecord-api:1.16-R0.5-SNAPSHOT")
    testCompile("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
}
