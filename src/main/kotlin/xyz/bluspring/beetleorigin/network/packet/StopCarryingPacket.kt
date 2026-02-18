package xyz.bluspring.beetleorigin.network.packet

import net.minecraft.core.UUIDUtil
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import xyz.bluspring.beetleorigin.network.BeetleNetwork
import java.util.*

data class StopCarryingPacket(
    val carrier: UUID
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return BeetleNetwork.STOP_CARRYING
    }

    companion object {
        val CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, StopCarryingPacket::carrier,
            ::StopCarryingPacket
        )
    }
}
