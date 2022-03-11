package com.github.shynixn.mccoroutine.bukkit.sample.listener;

import com.github.shynixn.mccoroutine.bukkit.sample.entity.UserData;
import com.github.shynixn.mccoroutine.bukkit.sample.impl.UserDataCache;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import java.util.concurrent.CompletionStage;

/**
 * This is a Java example how to interact with suspend functions from Java.
 */
public class EntityInteractListener implements Listener {
    private final UserDataCache userDataCache;

    public EntityInteractListener(UserDataCache userDataCache) {
        this.userDataCache = userDataCache;
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractAtEntityEvent event) {
        System.out.println("[EntityInteractListener] Is starting on Primary Thread: " + Bukkit.isPrimaryThread());

        CompletionStage<UserData> future = this.userDataCache.getUserDataFromPlayer(event.getPlayer());
        future.thenAccept(useData -> {
            System.out.println("[EntityInteractListener] Is ending on Primary Thread: " + Bukkit.isPrimaryThread());
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }
}
