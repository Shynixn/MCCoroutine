# Getting Started

In order to access the MCCoroutine Kotlin API, you need to include the following libraries into your project.

!!! note "Further help"
    Please take a look at the sample plugins ``mccoroutine-bukkit-sample`` or ``mccoroutine-sponge-sample`` which 
    can be found on [Github](https://github.com/Shynixn/MCCoroutine).
    A real production plugin using MCCoroutine can be found [here](https://github.com/Shynixn/BlockBall).

!!! note "Sponge Documentation"
    Please notice that these are the libraries for Bukkit-API based servers. If you are looking for the Sponge-API, simply
    replace bukkit with sponge in name of the dependencies. e.g. 'com.github.shynixn.mccoroutine:mccoroutine-sponge-api:
    x.x.x' - [Sponge Documentation](https://github.com/Shynixn/MCCoroutine/blob/master/SPONGE.md)

### 1. Add MCCoroutine

**Gradle**

```groovy
dependencies {
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:1.6.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:1.6.0")
}
```

**Maven**

```xml

<dependency>
    <groupId>com.github.shynixn.mccoroutine</groupId>
    <artifactId>mccoroutine-bukkit-api</artifactId>
    <version>1.6.0</version>
    <scope>compile</scope>
</dependency>
<dependency>
    <groupId>com.github.shynixn.mccoroutine</groupId>
    <artifactId>mccoroutine-bukkit-core</artifactId>
    <version>1.6.0</version>
    <scope>compile</scope>
</dependency>
```

### 2. Add the official Kotlin Coroutines libraries

MCCoroutine builds against Kotlin 1.5.x but does not ship the Kotlin Runtime or Kotlin Coroutines Runtime. This means
you can choose any Kotlin Runtime version as you like as long it is >= 1.3.0.

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

### 3. Shade the dependencies into your plugin.jar file

* For version >= 1.17: Add the kotlin and coroutine dependencies to the libraries tag

**plugin.yml**
```yaml
libraries:
  - com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:1.6.0
  - com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:1.6.0
```

* For version < 1.17: If you are using Kotlin, you probably know how shading dependencies works


### 4. Test if everything is working

Try to call ``launch{}`` in your ``onEnable()`` function in your ``Plugin`` class.
