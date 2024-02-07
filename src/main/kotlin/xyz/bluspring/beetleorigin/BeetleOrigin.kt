package xyz.bluspring.beetleorigin

import io.github.apace100.apoli.power.factory.condition.ConditionFactory
import io.github.apace100.apoli.registry.ApoliRegistries
import io.github.apace100.calio.data.SerializableData
import io.github.apace100.calio.data.SerializableDataType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.minecraft.ChatFormatting
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.InteractionResult
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.Item
import xyz.bluspring.beetleorigin.carry.CarryManager
import xyz.bluspring.beetleorigin.network.BeetleNetwork

class BeetleOrigin : ModInitializer {
    override fun onInitialize() {
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation(MOD_ID, "beetle"), Item(
            Item.Properties()
                .food(
                    FoodProperties.Builder()
                        .alwaysEat()
                        .fast()
                        .nutrition(1)
                        .effect(MobEffectInstance(MobEffects.CONFUSION, 15, 2), 0.75f)
                        .build()
                )
        ))

        val hasTag = ResourceLocation(MOD_ID, "is_of")
        Registry.register(ApoliRegistries.ITEM_CONDITION, hasTag, ConditionFactory(hasTag, SerializableData()
            .add("tag", SerializableDataType.tag(Registries.ITEM))) { data, stack ->
            val tag = data.get<TagKey<Item>>("tag")

            stack.`is`(tag)
        })

        UseEntityCallback.EVENT.register { player, level, hand, entity, hit ->
            if (BeetlePowers.CARRY_POWER.get(player) != null && player.isShiftKeyDown) {
                if (!player.getItemInHand(hand).isEmpty)
                    return@register InteractionResult.PASS

                val carryManager = CarryManager.get(level.isClientSide())

                if (carryManager.carriers.containsKey(player)) {
                    player.displayClientMessage(Component.literal("You're already carrying an entity!")
                        .withStyle(ChatFormatting.RED), true)
                    return@register InteractionResult.SUCCESS
                }

                if (carryManager.carriers.containsValue(entity)) {
                    player.displayClientMessage(Component.literal("Entity is already being carried!")
                        .withStyle(ChatFormatting.RED), true)
                    return@register InteractionResult.SUCCESS
                }

                if (!level.isClientSide) // Server must validate first
                    carryManager.carryEntity(player, entity)

                return@register InteractionResult.SUCCESS
            }

            InteractionResult.PASS
        }

        ServerLifecycleEvents.SERVER_STARTING.register {
            CarryManager.create(false)
        }

        ServerLifecycleEvents.SERVER_STOPPING.register {
            CarryManager.reset(false)
        }

        BeetleNetwork.initServer()
    }

    companion object {
        const val MOD_ID = "beetleorigin"
    }
}