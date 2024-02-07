package xyz.bluspring.beetleorigin.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.beetleorigin.carry.PlayerCarryExtension;

@Mixin(Player.class)
public class PlayerMixin implements PlayerCarryExtension {
    @Unique private int sneakTime;

    @Override
    public int getBeetleSneakTime() {
        return sneakTime;
    }

    @Override
    public void setBeetleSneakTime(int i) {
        sneakTime = i;
    }
}
