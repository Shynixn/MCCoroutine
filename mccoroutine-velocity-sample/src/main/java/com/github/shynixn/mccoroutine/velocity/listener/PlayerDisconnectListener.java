package com.github.shynixn.mccoroutine.velocity.listener;

import com.github.shynixn.mccoroutine.velocity.entity.UserData;
import com.github.shynixn.mccoroutine.velocity.impl.UserDataCache;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;

import java.util.concurrent.CompletionStage;

/**
 * This is a Java example how to interact with suspend functions from Java.
 */
public class PlayerDisconnectListener {
    private final UserDataCache userDataCache;

    public PlayerDisconnectListener(UserDataCache userDataCache) {
        this.userDataCache = userDataCache;
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        System.out.println("[PlayerDisconnectListener/onPlayerDisconnect] Is starting on Thread:" + Thread.currentThread().getName() + "" + Thread.currentThread().getId());

        CompletionStage<UserData> future = this.userDataCache.getUserDataFromPlayer(event.getPlayer());
        future.thenAccept(useData -> {
            System.out.println("[PlayerDisconnectListener/onPlayerDisconnect] Is ending on Thread:" + Thread.currentThread().getName() + "" + Thread.currentThread().getId());
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }
}
