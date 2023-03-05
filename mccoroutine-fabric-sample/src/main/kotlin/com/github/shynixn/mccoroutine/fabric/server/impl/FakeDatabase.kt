package com.github.shynixn.mccoroutine.fabric.server.impl

import com.github.shynixn.mccoroutine.fabric.server.entity.UserData
import net.minecraft.entity.player.PlayerEntity

class FakeDatabase {
    /**
     *  Simulates a getUserData call to a real database by delaying the result.
     */
    fun getUserDataFromPlayer(player: PlayerEntity): UserData {
        Thread.sleep(5000)
        val userData = UserData()
        userData.amountOfEntityKills = 20
        userData.amountOfPlayerKills = 30
        return userData
    }

    /**
     * Simulates a save User data call.
     */
    fun saveUserData(userData: UserData) {
        Thread.sleep(6000)
    }
}
