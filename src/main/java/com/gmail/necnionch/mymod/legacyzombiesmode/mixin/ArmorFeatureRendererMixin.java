package com.gmail.necnionch.mymod.legacyzombiesmode.mixin;

import com.gmail.necnionch.mymod.legacyzombiesmode.ZombiesModeMod;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorFeatureRenderer.class)
public class ArmorFeatureRendererMixin {
    private final ZombiesModeMod mod = ZombiesModeMod.getInstance();

    @Inject(
            method = "renderFeature",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/entity/Entity;FFFFFF)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void zm$renderFeature(LivingEntity entity, float handSwing, float handSwingAmount, float tickDelta, float age, float headYaw, float headPitch, float scale, int slot, CallbackInfo ci) {
        if (!mod.inGame() || mod.isDisablePlayerVisibility() || !(entity instanceof PlayerEntity)) {
            return;
        }

        ClientPlayerEntity me = MinecraftClient.getInstance().player;
        if (entity.isInvisible() || me.squaredDistanceTo(entity) > ZombiesModeMod.PLAYER_INVISIBLE_DISTANCE)
            return;

        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, ZombiesModeMod.PLAYER_INVISIBLE_ALPHA);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.alphaFunc(516, 0.003921569F);
    }

    @Inject(
            method = "renderFeature",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/entity/Entity;FFFFFF)V",
                    shift = At.Shift.AFTER
            )
    )
    private void zm$renderFeatureFinal(LivingEntity entity, float handSwing, float handSwingAmount, float tickDelta, float age, float headYaw, float headPitch, float scale, int slot, CallbackInfo ci) {
        if (!mod.inGame() || mod.isDisablePlayerVisibility() || !(entity instanceof PlayerEntity)) {
            return;
        }

        ClientPlayerEntity me = MinecraftClient.getInstance().player;
        if (entity.isInvisible() || me.squaredDistanceTo(entity) > 2)
            return;

        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);
    }

}
