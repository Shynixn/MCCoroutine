package com.github.shynixn.mccoroutine.sponge.sample.listener;

import com.github.shynixn.mccoroutine.sponge.sample.entity.UserData;
import com.github.shynixn.mccoroutine.sponge.sample.impl.UserDataCache;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.First;

import java.util.concurrent.CompletionStage;

/**
 * This is a Java example how to interact with suspend functions from Java.
 */
public class EntityInteractListener {
    private final UserDataCache userDataCache;

    public EntityInteractListener(UserDataCache userDataCache) {
        this.userDataCache = userDataCache;
    }

    @Listener
    public void onPlayerInteractEvent(InteractEntityEvent.Secondary event, @First(typeFilter = Player.class) Player player) {
        System.out.println("[EntityInteractListener] Is starting on Primary Thread: " + Sponge.getServer().isMainThread());

        CompletionStage<UserData> future = this.userDataCache.getUserDataFromPlayer(player);
        future.thenAccept(useData -> {
            System.out.println("[EntityInteractListener] Is ending on Primary Thread: " + Sponge.getServer().isMainThread());
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }
}
