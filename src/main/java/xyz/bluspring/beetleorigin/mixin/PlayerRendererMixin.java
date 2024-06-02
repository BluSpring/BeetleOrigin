package xyz.bluspring.beetleorigin.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.beetleorigin.carry.CarryManager;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {
    @Inject(method = "getArmPose", at = @At("RETURN"), cancellable = true)
    private static void beetleorigin$useCarryAnimation(AbstractClientPlayer player, InteractionHand hand, CallbackInfoReturnable<HumanoidModel.ArmPose> cir) {
        try {
            if (!CarryManager.Companion.get(true).isCarrying(player))
                return;

            var originalPose = cir.getReturnValue();

            if (originalPose == HumanoidModel.ArmPose.EMPTY || originalPose == HumanoidModel.ArmPose.ITEM) {
                cir.setReturnValue(HumanoidModel.ArmPose.THROW_SPEAR);
            }
        } catch (Exception ignored) {}
    }
}
