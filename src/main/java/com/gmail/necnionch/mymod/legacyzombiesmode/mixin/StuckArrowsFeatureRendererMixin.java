package com.gmail.necnionch.mymod.legacyzombiesmode.mixin;

import com.gmail.necnionch.mymod.legacyzombiesmode.ZombiesModeMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.feature.StuckArrowsFeatureRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StuckArrowsFeatureRenderer.class)
public class StuckArrowsFeatureRendererMixin {
    private final ZombiesModeMod mod = ZombiesModeMod.getInstance();

    @Inject(
            method = "render",
            at = @At("HEAD"),
            cancellable = true
    )
    private void zm$render(LivingEntity entity, float handSwing, float handSwingAmount, float tickDelta, float age, float headYaw, float headPitch, float scale, CallbackInfo ci) {
        if (!mod.inGame() || mod.isDisablePlayerVisibility() || !(entity instanceof PlayerEntity)) {
            return;
        }

        ClientPlayerEntity me = MinecraftClient.getInstance().player;
        if (entity.isInvisible() || me.squaredDistanceTo(entity) > ZombiesModeMod.PLAYER_INVISIBLE_DISTANCE)
            return;

        ci.cancel();
    }

}
