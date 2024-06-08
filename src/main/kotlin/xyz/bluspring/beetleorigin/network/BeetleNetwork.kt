package xyz.bluspring.beetleorigin.network

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import xyz.bluspring.beetleorigin.BeetleOrigin
import xyz.bluspring.beetleorigin.carry.CarryManager

object BeetleNetwork {
    val START_CARRYING = ResourceLocation(BeetleOrigin.MOD_ID, "start_carry")
    val STOP_CARRYING = ResourceLocation(BeetleOrigin.MOD_ID, "stop_carry")
    val THROW_CARRIED = ResourceLocation(BeetleOrigin.MOD_ID, "throw_carried")
    val SYNC_CARRY = ResourceLocation(BeetleOrigin.MOD_ID, "sync_carry")

    lateinit var server: MinecraftServer

    fun initClient() {
        ClientPlayNetworking.registerGlobalReceiver(START_CARRYING) { client, handler, buf, sender ->
            val carrierUuid = buf.readUUID()
            val carriedId = buf.readVarInt()

            val carrier = client.level!!.getPlayerByUUID(carrierUuid) ?: return@registerGlobalReceiver
            val carried = client.level!!.getEntity(carriedId) ?: return@registerGlobalReceiver

            val carryManager = CarryManager.get(true)
            carryManager.carryEntity(carrier, carried)
        }

        ClientPlayNetworking.registerGlobalReceiver(STOP_CARRYING) { client, handler, buf, sender ->
            val carrierUuid = buf.readUUID()

            val carrier = client.level!!.getPlayerByUUID(carrierUuid) ?: return@registerGlobalReceiver

            val carryManager = CarryManager.get(true)
            carryManager.stopCarrying(carrier)
        }

        ClientPlayNetworking.registerGlobalReceiver(THROW_CARRIED) { client, handler, buf, sender ->
            val x = buf.readDouble()
            val y = buf.readDouble()
            val z = buf.readDouble()

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

        ServerPlayNetworking.registerGlobalReceiver(THROW_CARRIED) { server, player, handler, buf, sender ->
            val carryManager = CarryManager.get(false)

            server.execute {
                carryManager.throwEntity(player)
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(SYNC_CARRY) { server, player, handler, buf, sender ->
            val entityId = buf.readVarInt()
            val entity = player.level().getEntity(entityId) ?: return@registerGlobalReceiver

            val carryManager = CarryManager.get(false)

            if (carryManager.isBeingCarried(entity)) {
                val carriedBuf = PacketByteBufs.create()

                carriedBuf.writeUUID(carryManager.getCarrier(entity)!!.uuid)
                carriedBuf.writeVarInt(entity.id)

                ServerPlayNetworking.send(player, START_CARRYING, carriedBuf)
            } else if (entity is Player && carryManager.isCarrying(entity)) {
                val carried = carryManager.getCarried(entity)!!

                val carriedBuf = PacketByteBufs.create()
                carriedBuf.writeUUID(entity.uuid)
                carriedBuf.writeVarInt(carried.id)

                ServerPlayNetworking.send(player, START_CARRYING, carriedBuf)
            }
        }
    }

    fun broadcast(id: ResourceLocation, data: FriendlyByteBuf, tracked: Entity) {
        val tracking = PlayerLookup.tracking(tracked)

        for (player in tracking) {
            ServerPlayNetworking.send(player, id, data)
        }

        if (tracked is ServerPlayer && !tracking.contains(tracked)) {
            ServerPlayNetworking.send(tracked, id, data)
        }
    }
}