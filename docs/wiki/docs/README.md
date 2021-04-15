# Introduction

MCCoroutine is library which adds extensive support for Kotlin Coroutines on Minecraft Server implementing the **Bukkit-API** or **Sponge-API**.

Examples for supported frameworks:

* Spigot
* Paper
* CraftBukkit
* SpongeVanilla
* SpongeForge

## Installation 

#### 1. Add the Coroutine libraries from Jetbrains (replace 1.x.x with your Kotlin version)

**Maven**
```xml
<dependency>
     <groupId>org.jetbrains.kotlinx</groupId>
     <artifactId>kotlinx-coroutines-core</artifactId>
     <version>1.x.x</version> 
     <scope>compile</scope>
</dependency>
<dependency>
     <groupId>org.jetbrains.kotlinx</groupId>
     <artifactId>kotlinx-coroutines-jdk8</artifactId>
     <version>1.x.x</version> 
     <scope>compile</scope>
</dependency>
```
**Gradle**

```groovy
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.x.x")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.x.x")
}
```

#### 2. Add the MCCoroutine libraries 

!!! warning
Below you can find the dependency for the **Bukkit-API**. See the following link for
the **Sponge-API**.

**Maven**
```xml
<dependency>
    <groupId>com.github.shynixn.mccoroutine</groupId>
    <artifactId>mccoroutine-bukkit-api</artifactId>
    <version>1.2.0</version>
    <scope>compile</scope>
</dependency>
<dependency>
    <groupId>com.github.shynixn.mccoroutine</groupId>
    <artifactId>mccoroutine-bukkit-core</artifactId>
    <version>1.2.0</version>
    <scope>compile</scope>
</dependency>
```
**Gradle**

```groovy
dependencies {
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:1.2.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:1.2.0")
}
```
