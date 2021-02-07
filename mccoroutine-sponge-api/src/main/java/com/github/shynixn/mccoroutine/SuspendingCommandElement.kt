package com.github.shynixn.mccoroutine

import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.ArgumentParseException
import org.spongepowered.api.command.args.CommandArgs
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.args.CommandElement
import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.text.Text

abstract class SuspendingCommandElement(pluginContainer: PluginContainer, text: Text) {
    internal val commandElement = object : CommandElement(text) {
        /**
         * Attempt to extract a value for this element from the given arguments.
         * This method is expected to have no side-effects for the source, meaning
         * that executing it will not change the state of the [CommandSource]
         * in any way.
         *
         * @param source The source to parse for
         * @param args the arguments
         * @return The extracted value
         * @throws ArgumentParseException if unable to extract a value
         */
        override fun parseValue(source: CommandSource, args: CommandArgs): Any? {
            var parsedValue: Any? = null

            pluginContainer.launch {
                parsedValue = this@SuspendingCommandElement.parseValue(source, args)
            }

            return parsedValue
        }

        /**
         * Fetch completions for command arguments.
         *
         * @param src The source requesting tab completions
         * @param args The arguments currently provided
         * @param context The context to store state in
         * @return Any relevant completions
         */
        override fun complete(src: CommandSource, args: CommandArgs, context: CommandContext): List<String?>? {
            var parsedValue: List<String?>? = null

            pluginContainer.launch {
                parsedValue = this@SuspendingCommandElement.complete(src, args, context)
            }

            return parsedValue
        }
    }

    /**
     * Return the key to be used for this object.
     *
     * @return the user-facing representation of the key
     */
    open fun getKey(): Text? {
        return commandElement.key
    }

    /**
     * Return the plain key, to be used when looking up this command element in
     * a [CommandContext]. If the key is a [TranslatableText], this
     * is the translation's id. Otherwise, this is the result of
     * [Text.toPlain].
     *
     * @return the raw key
     */
    open fun getUntranslatedKey(): String? {
        return commandElement.untranslatedKey
    }

    /**
     * Attempt to extract a value for this element from the given arguments and
     * put it in the given context. This method normally delegates to
     * [.parseValue] for getting the values.
     * This method is expected to have no side-effects for the source, meaning
     * that executing it will not change the state of the [CommandSource]
     * in any way.
     *
     * @param source The source to parse for
     * @param args The args to extract from
     * @param context The context to supply to
     * @throws ArgumentParseException if unable to extract a value
     */
    @Throws(ArgumentParseException::class)
    open fun parse(source: CommandSource, args: CommandArgs, context: CommandContext) {
        return commandElement.parse(source, args, context)
    }

    /**
     * Attempt to extract a value for this element from the given arguments.
     * This method is expected to have no side-effects for the source, meaning
     * that executing it will not change the state of the [CommandSource]
     * in any way.
     *
     * @param source The source to parse for
     * @param args the arguments
     * @return The extracted value
     * @throws ArgumentParseException if unable to extract a value
     */
    @Throws(ArgumentParseException::class)
    protected abstract suspend fun parseValue(source: CommandSource?, args: CommandArgs?): Any?

    /**
     * Fetch completions for command arguments.
     *
     * @param src The source requesting tab completions
     * @param args The arguments currently provided
     * @param context The context to store state in
     * @return Any relevant completions
     */
    abstract suspend fun complete(src: CommandSource?, args: CommandArgs?, context: CommandContext?): List<String?>?

    /**
     * Return a usage message for this specific argument.
     *
     * @param src The source requesting usage
     * @return The formatted usage
     */
    open fun getUsage(src: CommandSource): Text? {
        return commandElement.getUsage(src)
    }
}
