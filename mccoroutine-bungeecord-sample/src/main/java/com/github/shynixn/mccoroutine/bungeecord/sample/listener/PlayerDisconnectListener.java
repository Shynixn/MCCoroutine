package com.github.shynixn.mccoroutine.bungeecord.sample.listener;

import com.github.shynixn.mccoroutine.bungeecord.sample.entity.UserData;
import com.github.shynixn.mccoroutine.bungeecord.sample.impl.UserDataCache;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.concurrent.CompletionStage;

/**
 * This is a Java example how to interact with suspend functions from Java.
 */
public class PlayerDisconnectListener implements Listener {
    private final UserDataCache userDataCache;

    public PlayerDisconnectListener(UserDataCache userDataCache) {
        this.userDataCache = userDataCache;
    }

    @EventHandler
    public void onPlayerDisconnect(ServerDisconnectEvent event) {
        System.out.println("[PlayerConnectListener/onPlayerJoinEvent] Is starting on Thread:" + Thread.currentThread().getName() + "" + Thread.currentThread().getId());

        CompletionStage<UserData> future = this.userDataCache.getUserDataFromPlayer(event.getPlayer());
        future.thenAccept(useData -> {
            System.out.println("[PlayerConnectListener/onPlayerJoinEvent] Is ending on Thread:" + Thread.currentThread().getName() + "" + Thread.currentThread().getId());
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }
}
