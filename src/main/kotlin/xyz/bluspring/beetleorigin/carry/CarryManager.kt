package xyz.bluspring.beetleorigin.carry

import dev.architectury.event.EventResult
import dev.architectury.event.events.common.EntityEvent
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import xyz.bluspring.beetleorigin.network.BeetleNetwork

class CarryManager {
    val carriers = mutableMapOf<Player, Entity>()
    var isActive = true

    init {
        EntityEvent.LIVING_DEATH.register(this::onLivingDeath)
        ServerPlayConnectionEvents.DISCONNECT.register(this::onDisconnect)
    }

    private fun onLivingDeath(entity: Entity, source: DamageSource): EventResult {
        if (carriers.containsKey(entity)) {
            stopCarrying(entity as Player)
        } else if (carriers.containsValue(entity)) {
            val key = carriers.filter { it.value == entity }.keys.first()
            stopCarrying(key)
        }

        return EventResult.pass()
    }

    private fun onDisconnect(handler: ServerGamePacketListenerImpl, server: MinecraftServer) {
        if (!isActive)
            return

        stopCarrying(handler.player)
    }

    fun reset() {
        isActive = false
        EntityEvent.LIVING_DEATH.unregister(this::onLivingDeath)
    }

    fun carryEntity(carrier: Player, carried: Entity) {
        if (carriers.containsKey(carrier))
            throw IllegalStateException("$carrier is already carrying an entity ($carried)!")

        if (carriers.containsValue(carried))
            throw IllegalStateException("$carried is already being carried!")

        carriers[carrier] = carried
    }

    fun stopCarrying(carrier: Player) {
        if (!carrier.level().isClientSide) {
            val buf = PacketByteBufs.create()
            buf.writeUUID(carrier.uuid)

            BeetleNetwork.broadcast(BeetleNetwork.STOP_CARRYING, buf)
        }

        carriers.remove(carrier)
    }

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