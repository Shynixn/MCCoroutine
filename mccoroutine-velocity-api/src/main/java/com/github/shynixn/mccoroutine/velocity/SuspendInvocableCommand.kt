package com.github.shynixn.mccoroutine.velocity

import com.google.common.collect.ImmutableList
import com.velocitypowered.api.command.Command
import com.velocitypowered.api.command.CommandInvocation

interface SuspendInvocableCommand<I : CommandInvocation<*>> {
    /**
     * Executes the command for the specified invocation.
     *
     * @param invocation the invocation context
     */
    suspend fun execute(invocation: I)

    /**
     * Provides tab complete suggestions for the specified invocation.
     *
     * @param invocation the invocation context
     * @return the tab complete suggestions
     */
    suspend fun suggest(invocation: I): List<String> {
        return ImmutableList.of()
    }

    /**
     * Tests to check if the source has permission to perform the specified invocation.
     *
     *
     * If the method returns `false`, the handling is forwarded onto
     * the players current server.
     *
     * @param invocation the invocation context
     * @return `true` if the source has permission
     */
    fun hasPermission(invocation: I): Boolean {
        return true
    }
}
