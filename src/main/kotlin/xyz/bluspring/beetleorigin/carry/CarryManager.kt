package xyz.bluspring.beetleorigin.carry

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player

class CarryManager {
    val carriers = mutableMapOf<Player, Entity>()



    companion object {
        var client: CarryManager? = null
        var server: CarryManager? = null

        fun get(isClient: Boolean): CarryManager {
            return if (isClient)
                client!!
            else
                server!!
        }

        fun create(isClient: Boolean): CarryManager {
            val manager = CarryManager()

            if (isClient)
                client = manager
            else
                server = manager

            return manager
        }

        fun reset(isClient: Boolean) {
            if (isClient)
                client = null
            else
                server = null
        }
    }
}