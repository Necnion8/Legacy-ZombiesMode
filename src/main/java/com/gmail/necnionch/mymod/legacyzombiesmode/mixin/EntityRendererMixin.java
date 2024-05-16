package com.gmail.necnionch.mymod.legacyzombiesmode.mixin;

import com.gmail.necnionch.mymod.legacyzombiesmode.ZombiesModeMod;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    private final ZombiesModeMod mod = ZombiesModeMod.getInstance();

    @Inject(
            method = "renderFire",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/GlStateManager;color(FFFF)V",
                    shift = At.Shift.AFTER
            )
    )
    private void zm$renderFire(Entity entity, double x, double y, double z, float f, CallbackInfo ci) {
        if (!mod.inGame() || mod.isDisablePlayerVisibility() || !(entity instanceof PlayerEntity)) {
            return;
        }

        ClientPlayerEntity me = MinecraftClient.getInstance().player;
        if (entity.isInvisible() || me.squaredDistanceTo(entity) > ZombiesModeMod.PLAYER_INVISIBLE_DISTANCE)
            return;

        GlStateManager.color(1f, 1f, 1f, ZombiesModeMod.PLAYER_INVISIBLE_ALPHA);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.alphaFunc(516, 0.003921569F);

    }

    @Inject(
            method = "renderFire",
            at = @At("RETURN")
    )
    private void zm$renderFireFinal(Entity entity, double x, double y, double z, float f, CallbackInfo ci) {
        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.depthMask(true);
    }

}
