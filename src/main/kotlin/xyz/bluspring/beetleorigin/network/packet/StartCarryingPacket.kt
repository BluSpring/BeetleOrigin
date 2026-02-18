package xyz.bluspring.beetleorigin.network.packet

import net.minecraft.core.UUIDUtil
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import xyz.bluspring.beetleorigin.network.BeetleNetwork
import java.util.*

data class StartCarryingPacket(
    val carrier: UUID,
    val carried: Int
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return BeetleNetwork.START_CARRYING
    }

    companion object {
        val CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, StartCarryingPacket::carrier,
            ByteBufCodecs.VAR_INT, StartCarryingPacket::carried,
            ::StartCarryingPacket
        )
    }
}
