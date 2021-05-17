# Getting Started

In order to access the MCCoroutine Kotlin API, you need to include the following libraries to your project.

!!! note "Kotlin Coroutines Description"
    Please notice that these are the libraries for Bukkit-API based servers. If you are looking for the Sponge-API, simply
    replace bukkit with sponge in name of the dependencies. e.g. 'com.github.shynixn.mccoroutine:mccoroutine-sponge-api:
    x.x.x'

### 1. Add MCCoroutine

**Gradle**

```groovy
dependencies {
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:1.2.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:1.2.0")
}
```

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

### 2. Add the official Kotlin Coroutines libraries

MCCoroutine builds against Kotlin 1.3.x but does not ship the Kotlin Runtime or Kotlin Coroutines Runtime. This means
you are free to choose any ship your Kotlin Runtime as you like as long it is >= 1.3.0.

Replace 1.x.x with the actual versions. 

**Gradle**

```groovy
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.x.x")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.x.x")
}
```

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

### 3. Test if everything is working

Try to call ``launch{}`` in your ``onEnable()`` function in your ``Plugin`` class.
