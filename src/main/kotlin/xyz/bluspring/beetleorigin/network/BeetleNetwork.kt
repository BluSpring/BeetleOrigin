package xyz.bluspring.beetleorigin.network

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import xyz.bluspring.beetleorigin.BeetleOrigin

object BeetleNetwork {
    val START_CARRYING = ResourceLocation(BeetleOrigin.MOD_ID, "start_carry")
    val STOP_CARRYING = ResourceLocation(BeetleOrigin.MOD_ID, "stop_carry")

    lateinit var server: MinecraftServer

    fun initClient() {
        ClientPlayNetworking.registerGlobalReceiver(START_CARRYING) { client, handler, buf, sender ->

        }

        ClientPlayNetworking.registerGlobalReceiver(STOP_CARRYING) { client, handler, buf, sender ->

        }
    }

    fun initServer() {
        ServerLifecycleEvents.SERVER_STARTING.register {
            server = it
        }
    }

    fun broadcast(id: ResourceLocation, data: FriendlyByteBuf) {
        for (player in server.playerList.players) {
            ServerPlayNetworking.send(player, id, data)
        }
    }
}