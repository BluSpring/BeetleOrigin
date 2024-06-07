package xyz.bluspring.beetleorigin.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.beetleorigin.carry.CarryManager;
import xyz.bluspring.beetleorigin.carry.PlayerCarryExtension;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements PlayerCarryExtension {
    @Unique private int sneakTime;

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public int getBeetleSneakTime() {
        return sneakTime;
    }

    @Override
    public void setBeetleSneakTime(int i) {
        sneakTime = i;
    }

    // pro-tip: maybe cancel this
    @Inject(method = "wantsToStopRiding", at = @At("HEAD"), cancellable = true)
    private void disableExitIfBeetleRiding(CallbackInfoReturnable<Boolean> cir) {
        if (CarryManager.Companion.get(this.level().isClientSide()).isBeingCarried(this))
            cir.setReturnValue(false);
    }
}
