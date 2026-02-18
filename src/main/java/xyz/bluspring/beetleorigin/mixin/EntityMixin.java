package xyz.bluspring.beetleorigin.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.beetleorigin.carry.EntityTossExtension;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityTossExtension {
    @Shadow public abstract void resetFallDistance();

    @ModifyReturnValue(method = "getPassengerAttachmentPoint", at = @At("RETURN"))
    private Vec3 beetleorigin$changePlayerPassengerOffset(Vec3 original) {
        if ((Object) this instanceof Player)
            return original.add(0.0, 0.4, 0.0);
        else
            return original;
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
