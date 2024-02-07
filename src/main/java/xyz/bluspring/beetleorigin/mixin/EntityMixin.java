package xyz.bluspring.beetleorigin.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "getPassengersRidingOffset", at = @At("RETURN"), cancellable = true)
    private void beetleorigin$changePlayerPassengerOffset(CallbackInfoReturnable<Double> cir) {
        if ((Object) this instanceof Player) {
            cir.setReturnValue(cir.getReturnValueD() + 0.4);
        }
    }
}
