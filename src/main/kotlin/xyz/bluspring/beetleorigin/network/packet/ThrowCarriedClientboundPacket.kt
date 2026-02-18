package xyz.bluspring.beetleorigin.network.packet

import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import org.joml.Vector3d
import org.joml.Vector3dc
import xyz.bluspring.beetleorigin.network.BeetleNetwork

data class ThrowCarriedClientboundPacket(
    val direction: Vector3d
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return BeetleNetwork.THROW_CARRIED_CLIENTBOUND
    }

    companion object {
        val VEC3_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, Vector3dc::x,
            ByteBufCodecs.DOUBLE, Vector3dc::y,
            ByteBufCodecs.DOUBLE, Vector3dc::z,
            ::Vector3d
        )

        val CODEC = StreamCodec.composite(
            VEC3_CODEC, ThrowCarriedClientboundPacket::direction,
            ::ThrowCarriedClientboundPacket
        )
    }
}
