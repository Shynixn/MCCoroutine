package com.github.shynixn.mccoroutine.minestom.sample.server.impl

import com.github.shynixn.mccoroutine.minestom.sample.server.entity.UserData
import net.minestom.server.entity.Player

class FakeDatabase {
    /**
     *  Simulates a getUserData call to a real database by delaying the result.
     */
    fun getUserDataFromPlayer(player: Player): UserData {
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
