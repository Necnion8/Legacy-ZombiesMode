package com.gmail.necnionch.mymod.legacyzombiesmode.mixin;

import com.gmail.necnionch.mymod.legacyzombiesmode.ZombiesModeMod;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    private final ZombiesModeMod mod = ZombiesModeMod.getInstance();
    @Shadow protected EntityModel model;

    @Inject(
            method = "renderModel",
            at = @At("HEAD"),
            cancellable = true
    )
    private void zm$renderModel(LivingEntity entity, float f, float g, float h, float i, float j, float k, CallbackInfo ci) {
        if (!mod.inGame() || mod.isDisablePlayerVisibility() || !(entity instanceof PlayerEntity)) {
            return;
        }

        ClientPlayerEntity me = MinecraftClient.getInstance().player;
        if (entity.isInvisible() || me.squaredDistanceTo(entity) > ZombiesModeMod.PLAYER_INVISIBLE_DISTANCE)
            return;

        ci.cancel();

        if (!((EntityRendererAccessor) this).invokeBindTexture(entity))
            return;

        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, ZombiesModeMod.PLAYER_INVISIBLE_ALPHA);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.alphaFunc(516, 0.003921569F);

        this.model.render(entity, f, g, h, i, j, k);
        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);
    }

}
