package xyz.bluspring.beetleorigin

import io.github.apace100.apoli.power.Power
import io.github.apace100.apoli.power.PowerType
import io.github.apace100.apoli.power.PowerTypeReference
import net.minecraft.resources.ResourceLocation

object BeetlePowers {
    val CARRY_POWER: PowerType<*> = PowerTypeReference<Power>(ResourceLocation(BeetleOrigin.MOD_ID, "carry"))
}