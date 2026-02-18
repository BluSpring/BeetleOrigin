package xyz.bluspring.beetleorigin.network

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import xyz.bluspring.beetleorigin.BeetleOrigin
import xyz.bluspring.beetleorigin.carry.CarryManager
import xyz.bluspring.beetleorigin.network.packet.*

object BeetleNetwork {
    val START_CARRYING = CustomPacketPayload.Type<StartCarryingPacket>(ResourceLocation.fromNamespaceAndPath(BeetleOrigin.MOD_ID, "start_carry"))
    val STOP_CARRYING = CustomPacketPayload.Type<StopCarryingPacket>(ResourceLocation.fromNamespaceAndPath(BeetleOrigin.MOD_ID, "stop_carry"))
    val THROW_CARRIED_SERVERBOUND = CustomPacketPayload.Type<ThrowCarriedServerboundPacket>(ResourceLocation.fromNamespaceAndPath(BeetleOrigin.MOD_ID, "throw_carried"))
    val THROW_CARRIED_CLIENTBOUND = CustomPacketPayload.Type<ThrowCarriedClientboundPacket>(ResourceLocation.fromNamespaceAndPath(BeetleOrigin.MOD_ID, "throw_carried"))
    val SYNC_CARRY = CustomPacketPayload.Type<SyncCarryPacket>(ResourceLocation.fromNamespaceAndPath(BeetleOrigin.MOD_ID, "sync_carry"))

    lateinit var server: MinecraftServer

    init {
        PayloadTypeRegistry.playS2C().register(START_CARRYING, StartCarryingPacket.CODEC)
        PayloadTypeRegistry.playS2C().register(STOP_CARRYING, StopCarryingPacket.CODEC)
        PayloadTypeRegistry.playC2S().register(THROW_CARRIED_SERVERBOUND, ThrowCarriedServerboundPacket.CODEC)
        PayloadTypeRegistry.playS2C().register(THROW_CARRIED_CLIENTBOUND, ThrowCarriedClientboundPacket.CODEC)
        PayloadTypeRegistry.playC2S().register(SYNC_CARRY, SyncCarryPacket.CODEC)
    }

    fun initClient() {
        ClientPlayNetworking.registerGlobalReceiver(START_CARRYING) { packet, ctx ->
            val (carrierUuid, carriedId) = packet
            val client = ctx.client()

            val carrier = client.level!!.getPlayerByUUID(carrierUuid) ?: return@registerGlobalReceiver
            val carried = client.level!!.getEntity(carriedId) ?: return@registerGlobalReceiver

            val carryManager = CarryManager.get(true)
            carryManager.carryEntity(carrier, carried)
        }

        ClientPlayNetworking.registerGlobalReceiver(STOP_CARRYING) { packet, ctx ->
            val (carrierUuid) = packet
            val client = ctx.client()

            val carrier = client.level!!.getPlayerByUUID(carrierUuid) ?: return@registerGlobalReceiver

            val carryManager = CarryManager.get(true)
            carryManager.stopCarrying(carrier)
        }

        ClientPlayNetworking.registerGlobalReceiver(THROW_CARRIED_CLIENTBOUND) { packet, ctx ->
            val client = ctx.client()
            val x = packet.direction.x
            val y = packet.direction.y
            val z = packet.direction.z

            client.execute {
                client.player?.setDeltaMovement(x, y, z)
                client.player?.hurtMarked = true
            }
        }

        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            CarryManager.create(true)
        }

        ClientPlayConnectionEvents.DISCONNECT.register { listener, client ->
            client.execute {
                CarryManager.reset(true)
            }
        }
    }

    fun initServer() {
        ServerLifecycleEvents.SERVER_STARTING.register {
            server = it
        }

        ServerPlayNetworking.registerGlobalReceiver(THROW_CARRIED_SERVERBOUND) { packet, ctx ->
            val player = ctx.player()
            val carryManager = CarryManager.get(false)

            server.execute {
                carryManager.throwEntity(player)
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(SYNC_CARRY) { packet, ctx ->
            val (entityId) = packet
            val player = ctx.player()
            val entity = player.level().getEntity(entityId) ?: return@registerGlobalReceiver

            val carryManager = CarryManager.get(false)

            if (carryManager.isBeingCarried(entity)) {
                ServerPlayNetworking.send(player, StartCarryingPacket(carryManager.getCarrier(entity)!!.uuid, entity.id))
            } else if (entity is Player && carryManager.isCarrying(entity)) {
                val carried = carryManager.getCarried(entity)!!
                ServerPlayNetworking.send(player, StartCarryingPacket(entity.uuid, carried.id))
            }
        }
    }

    fun broadcast(packet: CustomPacketPayload, tracked: Entity) {
        val tracking = PlayerLookup.tracking(tracked)

        for (player in tracking) {
            ServerPlayNetworking.send(player, packet)
        }

        if (tracked is ServerPlayer && !tracking.contains(tracked)) {
            ServerPlayNetworking.send(tracked, packet)
        }
    }
}