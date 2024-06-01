package xyz.bluspring.beetleorigin.carry

import dev.architectury.event.EventResult
import dev.architectury.event.events.common.EntityEvent
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.util.Mth
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import org.joml.Vector3d
import org.joml.Vector3f
import xyz.bluspring.beetleorigin.network.BeetleNetwork

class CarryManager(isClient: Boolean) {
    val carriers = mutableMapOf<Player, Entity>()
    var isActive = true

    init {
        EntityEvent.LIVING_DEATH.register(this::onLivingDeath)
        ServerPlayConnectionEvents.DISCONNECT.register(this::onDisconnect)

        if (isClient) {
            initClient()
        }
    }

    private fun initClient() {
        ClientEntityEvents.ENTITY_UNLOAD.register { entity, level ->
            if (entity is Player) {
                if (carriers.containsKey(entity))
                    carriers.remove(entity)
                else if (carriers.containsValue(entity)) {
                    val carrier = getCarrier(entity)
                    carriers.remove(carrier)
                }
            }
        }
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

        if (isCarrying(handler.player))
            stopCarrying(handler.player)
        else if (isBeingCarried(handler.player)) {
            val carrier = getCarrier(handler.player) ?: return
            stopCarrying(carrier)
        }
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
        if (!carrier.level().isClientSide) {
            val buf = PacketByteBufs.create()
            buf.writeUUID(carrier.uuid)
            buf.writeVarInt(carried.id)

            BeetleNetwork.broadcast(BeetleNetwork.START_CARRYING, buf)
        }

        carried.startRiding(carrier, true)
    }

    fun stopCarrying(carrier: Player) {
        if (!carrier.level().isClientSide) {
            val buf = PacketByteBufs.create()
            buf.writeUUID(carrier.uuid)

            BeetleNetwork.broadcast(BeetleNetwork.STOP_CARRYING, buf)
        }

        val carried = carriers[carrier] ?: return
        carriers.remove(carrier)

        carried.removeVehicle()
        carried.dismountTo(carrier.x, carrier.y + carrier.passengersRidingOffset + carried.myRidingOffset, carrier.z)
    }

    fun isCarrying(carrier: Player): Boolean {
        return carriers.containsKey(carrier)
    }

    fun isBeingCarried(carried: Entity): Boolean {
        return carriers.containsValue(carried)
    }

    fun getCarrier(carried: Entity): Player? {
        return carriers.filter { it.value == carried }.keys.firstOrNull()
    }

    fun getCarried(carrier: Player): Entity? {
        return carriers[carrier]
    }

    fun throwEntity(carrier: Player) {
        val carried = getCarried(carrier) ?: return

        stopCarrying(carrier)
        (carried as EntityTossExtension).beetleWasThrown = true

        val forwardVelocity = 5.4

        val radX = (carrier.xRot * Mth.DEG_TO_RAD) + Mth.HALF_PI
        val pitch = Mth.sin(radX)
        val radY = carrier.yRot * Mth.DEG_TO_RAD

        val vec3 = Vector3f(
            -(Mth.sin(radY)),
            1f,
            (Mth.cos(radY))
        ).normalize()

        val vec3d = Vector3d(
            forwardVelocity * pitch,
            3.4 * (Mth.cos(radX)),
            forwardVelocity * pitch
        ).mul(vec3)

        carried.setDeltaMovement(vec3d.x, vec3d.y, vec3d.z)

        if (carried is ServerPlayer) {
            carried.connection.send(ClientboundSetEntityMotionPacket(carried))
        }
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
            val manager = CarryManager(isClient)

            if (isClient)
                client = manager
            else
                server = manager

            return manager
        }

        fun reset(isClient: Boolean) {
            if (isClient) {
                client?.reset()
                client = null
            } else {
                server?.reset()
                server = null
            }
        }
    }
}