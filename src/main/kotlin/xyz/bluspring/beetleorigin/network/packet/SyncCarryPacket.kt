package xyz.bluspring.beetleorigin.network.packet

import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import xyz.bluspring.beetleorigin.network.BeetleNetwork

data class SyncCarryPacket(
    val entity: Int
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return BeetleNetwork.SYNC_CARRY
    }

    companion object {
        val CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncCarryPacket::entity,
            ::SyncCarryPacket
        )
    }
}
