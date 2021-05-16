# Creating a new Plugin

This guide deals with an example where a new plugin is developed from scratch using MCCoroutine to handle asynchronous
and synchronous code. MCCoroutine can be easily integrated into existing plugins but this page only deals with a new
plugin.

### 1. Understanding the goal

We want to create a new plugin which stores and retrieves the following user data into a database when a user enters our
Bukkit-API based server.

````
UUID
Player Name
Last Join Date
````

### 2. Include MCCoroutine and Kotlin Coroutines

This example plugin uses gradle as a build system. For more details see the ``Started Page``.

**Gradle**

```groovy
dependencies {
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:1.2.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:1.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.10")
}
```

### 3. Create the Java Plugin class

Here the first decision is to decide between extendin ``JavaPlugin`` or ``SuspendingJavaPlugin`` which is a new base
class extending ``JavaPlugin``.

Here is an easy rule to follow:

* If you want to perform async operations or call other suspending functions from your plugin class, go with the newly
  available type ``SuspendingJavaPlugin`` otherwise use ``JavaPlugin``.

````kotlin
class MCCoroutineSamplePlugin : SuspendingJavaPlugin() {
    override suspend fun onEnableAsync() {
    }

    override suspend fun onDisableAsync() {
    }
}
````

!!! note "How onEnableAsync works"
The implementation which calls the ``onEnableAsync`` function manipulates the Bukkit Server implementation in the
following way:

* If a context switch is made, it blocks the entire minecraft-server thread until the context is given back. This means
  in this method you can switch contexts as you like but the plugin is not considered enabled until the context is given
  back.
* It allows for a clean startup as the plugin is not considered "enabled" until the context is given back.
* Other plugins which are already enabled, may or may not already perform work in the background.
* Plugins which may get enabled in the future, wait until this plugin is enabled.

### 3. Create Database and Player data class

````kotlin
class PlayerData(var uuid: UUID, var name: String, var lastJoinDate: Date) {
}
````

Here, we perform all database operations on the IO context provided by Kotlin Coroutines.
The result is automatically returned to the Bukkit primary thread. 

````kotlin
class Database {
    suspend fun createDbIfNotExist() {
        withContext(Dispatchers.IO){
          // ... create tables
        }       
    }

    suspend fun PlayerData getDataFromPlayer(player : Player) {
        val playerData = withContext(Dispatchers.IO) {
            // ... get from database by player uuid
            PlayerData(uuid, name, lastJoinDate)
        }
    
        return playerData;
    }
  
    suspend fun saveData(player : Player, playerData : PlayerData) {
        withContext(Dispatchers.IO){
            // insert or update playerData
        }
    }
}
````

### 4. Connect JavaPlugin and Database

Test if the database is created on plugin startup and then continue on the next page.

````kotlin
class MCCoroutineSamplePlugin : SuspendingJavaPlugin() {
    private val database = DataBase()
  
    override suspend fun onEnableAsync() {
        database.createDbIfNotExist()
    }

    override suspend fun onDisableAsync() {
    }
}
````
