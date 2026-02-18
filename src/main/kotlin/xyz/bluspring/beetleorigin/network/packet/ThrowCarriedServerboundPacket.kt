package xyz.bluspring.beetleorigin.network.packet

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import xyz.bluspring.beetleorigin.network.BeetleNetwork

object ThrowCarriedServerboundPacket : CustomPacketPayload {
    val CODEC = StreamCodec.unit<ByteBuf, ThrowCarriedServerboundPacket>(ThrowCarriedServerboundPacket)

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return BeetleNetwork.THROW_CARRIED_SERVERBOUND
    }
}
