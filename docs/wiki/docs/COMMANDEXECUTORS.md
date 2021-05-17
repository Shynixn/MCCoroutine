# Adding Suspending CommandExecutors

This guide continues the guide 'Creating a new Plugin' and describes how command executors can be used to edit player
data.

### 1. Create the CommandExecutor class

Create a traditional CommandExecutor but implement ``SuspendingCommandExecutor`` instead of ``CommandExecutor``. Please
consider that the return value ``true`` is automatically assumed if the function is suspended in one branch.

````kotlin
class PlayerDataCommandExecutor(private val database: Database) : SuspendingCommandExecutor {
    override suspend fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) {
            return false
        }

        if (args.size == 2 && args[0].equals("rename", true)) {
            val name = args[1]
            val playerData = database.getDataFromPlayer(sender)
            playerData.name = name
            database.saveData(player, playerData)
            return true
        }

        return false
    }
}
````

### 2. Connect JavaPlugin and PlayerDataCommandExecutor

Instead of using ``setExecutor``, use the provided extension method ``setSuspendingExecutor`` to allow to register a
suspendable command executor.

````kotlin
class MCCoroutineSamplePlugin : SuspendingJavaPlugin() {
    private val database = Database()

    override suspend fun onEnableAsync() {
        database.createDbIfNotExist()
        server.pluginManager.registerSuspendingEvents(PlayerDataListener(database), plugin)
        getCommand("playerdata")!!.setSuspendingExecutor(PlayerDataCommandExecutor(database))
    }

    override suspend fun onDisableAsync() {
    }
}
````

### 3. Test the CommandExecutor

Join your server and enter the playerData command to edit your player data.

The next page continuous by adding repeating and delayed tasks to the plugin.
