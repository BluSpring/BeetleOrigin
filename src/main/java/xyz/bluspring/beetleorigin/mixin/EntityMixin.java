package xyz.bluspring.beetleorigin.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.beetleorigin.carry.EntityTossExtension;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityTossExtension {
    @Shadow public abstract void resetFallDistance();

    @Inject(method = "getPassengersRidingOffset", at = @At("RETURN"), cancellable = true)
    private void beetleorigin$changePlayerPassengerOffset(CallbackInfoReturnable<Double> cir) {
        if ((Object) this instanceof Player) {
            cir.setReturnValue(cir.getReturnValueD() + 0.4);
        }
    }

    @Unique private boolean wasThrown;

    @Override
    public boolean getBeetleWasThrown() {
        return wasThrown;
    }

    @Override
    public void setBeetleWasThrown(boolean b) {
        wasThrown = b;
    }

    @Inject(method = "checkFallDamage", at = @At("HEAD"))
    private void beetleorigin$disableFallDamageOnThrow(double y, boolean onGround, BlockState state, BlockPos pos, CallbackInfo ci) {
        if (onGround && wasThrown) {
            this.resetFallDistance();
            wasThrown = false;
        }
    }
}
