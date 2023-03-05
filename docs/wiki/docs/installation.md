# Getting Started

In order to use the MCCoroutine Kotlin API, you need to include the following libraries into your project.

## Add MCCoroutine Libraries

=== "Bukkit"

    ```groovy
    dependencies {
        implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.10.0")
        implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.10.0")
    }
    ```

=== "BungeeCord"

    ```groovy
    dependencies {
        implementation("com.github.shynixn.mccoroutine:mccoroutine-bungeecord-api:2.10.0")
        implementation("com.github.shynixn.mccoroutine:mccoroutine-bungeecord-core:2.10.0")
    }
    ```

=== "Sponge"

    ```groovy
    dependencies {
        implementation("com.github.shynixn.mccoroutine:mccoroutine-sponge-api:2.10.0")
        implementation("com.github.shynixn.mccoroutine:mccoroutine-sponge-core:2.10.0")
    }
    ```

=== "Velocity"

    ```groovy
    dependencies {
        implementation("com.github.shynixn.mccoroutine:mccoroutine-velocity-api:2.10.0")
        implementation("com.github.shynixn.mccoroutine:mccoroutine-velocity-core:2.10.0")
    }
    ```

=== "Minestom"

    ```groovy
    dependencies {
        implementation("com.github.shynixn.mccoroutine:mccoroutine-minestom-api:2.10.0")
        implementation("com.github.shynixn.mccoroutine:mccoroutine-minestom-core:2.10.0")
    }
    ```

=== "Fabric"

    ```groovy
    dependencies {
        implementation("com.github.shynixn.mccoroutine:mccoroutine-fabric-api:2.10.0")
        implementation("com.github.shynixn.mccoroutine:mccoroutine-fabric-core:2.10.0")
    }
    ```

## Add Kotlin Coroutines Libraries

MCCoroutine builds against Kotlin 1.3.x, however it does not distribute the Kotlin Runtime or Kotlin Coroutines Runtime.
This means, you can use any Kotlin version in your plugins. It is even encouraged to always use the latest version.

Replace 1.x.x with the actual versions. 

```groovy
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.x.x")
}
```

## Shade Dependencies

=== "Bukkit Server 1.17 - Latest"

    **plugin.yml**
    ```yaml
    libraries:
      - com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.10.0
      - com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.10.0
    ```

=== "Other Server"

    Shade the libraries into your plugin.jar file using gradle or maven. 


### Test the Plugin

Try to call ``launch{}`` in your ``onEnable()`` function in your ``Plugin`` class.

!!! note "Further help"
    Please take a look at the sample plugins (e.g. ``mccoroutine-bukkit-sample`` or ``mccoroutine-sponge-sample``) which
    can be found on [Github](https://github.com/Shynixn/MCCoroutine).
    A real production plugin using MCCoroutine can be found [here](https://github.com/Shynixn/BlockBall).
