package xyz.bluspring.beetleorigin.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.beetleorigin.carry.CarryManager;

@Mixin(Warden.class)
public abstract class WardenMixin extends Monster {
    protected WardenMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "canTargetEntity", at = @At("HEAD"), cancellable = true)
    private void disableTrackingIfCarrier(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!(entity instanceof Player player))
            return;

        var carrier = CarryManager.Companion.get(this.level().isClientSide()).getCarrier(this);

        if (carrier == null)
            return;

        if (carrier.getUUID().equals(entity.getUUID())) {
            cir.setReturnValue(false);
        }
    }
}
