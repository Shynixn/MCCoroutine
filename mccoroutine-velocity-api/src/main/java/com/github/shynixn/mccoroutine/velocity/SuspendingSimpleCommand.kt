package com.github.shynixn.mccoroutine.velocity

import com.velocitypowered.api.command.SimpleCommand

/**
 * Suspending SimpleCommand.
 */
interface SuspendingSimpleCommand : SuspendInvocableCommand<SimpleCommand.Invocation>
