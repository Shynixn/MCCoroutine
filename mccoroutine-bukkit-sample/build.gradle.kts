import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version ("2.0.4")
}

publishing {
    publications {
        (findByName("mavenJava") as MavenPublication).artifact(tasks.findByName("shadowJar")!!)
    }
}

tasks.withType<ShadowJar> {
    dependsOn("jar")
    archiveName = "$baseName-$version.$extension"

    // Change the output folder of the plugin.
    destinationDir = File("D:\\Benutzer\\Temp\\plugins")
}

dependencies {
    implementation(project(":mccoroutine-bukkit-core"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.72")

    
    compileOnly("org.spigotmc:spigot116R2:1.16.2-R2.0")
    testCompile("org.spigotmc:spigot116R2:1.16.2-R2.0")
}
