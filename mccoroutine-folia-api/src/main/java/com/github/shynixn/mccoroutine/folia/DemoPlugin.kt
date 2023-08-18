package com.github.shynixn.mccoroutine.folia

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.plugin.Plugin

class DemoPlugin  {
    private lateinit var plugin : Plugin

    fun demo(entity : Entity ){
        plugin.launch {
            withContext(plugin.entityDispatcher(entity)){
                entity.customName = "Change name"
                delay(50)
                entity.customName = "CustomName"
            }

            val entities: List<Entity>  = listOf()
            val entitiesWithResult = entities.map { e ->
                Pair(e, async(plugin.entityDispatcher(entity)) {
                    entity.location.block.type.name
                })
            }
        }


    }

}
