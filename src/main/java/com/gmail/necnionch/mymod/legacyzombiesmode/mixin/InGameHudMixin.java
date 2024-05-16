package com.gmail.necnionch.mymod.legacyzombiesmode.mixin;

import com.gmail.necnionch.mymod.legacyzombiesmode.ZombiesModeMod;
import com.gmail.necnionch.mymod.legacyzombiesmode.util.Util;
import com.gmail.necnionch.mymod.legacyzombiesmode.util.ZUtil;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = InGameHud.class)
public abstract class InGameHudMixin {
    private final ZombiesModeMod mod = ZombiesModeMod.getInstance();
    @Shadow @Final private ItemRenderer itemRenderer;
    @Shadow @Final private MinecraftClient client;

    @Shadow public abstract TextRenderer getFontRenderer();

    @Shadow private String overlayMessage;

    @Shadow private int overlayRemaining;

    private String modifiedOverlayMessageLast = "";

    @Inject(
            method = "renderHotbarItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/item/ItemRenderer;renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;II)V"
            ),
            cancellable = true
    )
    private void zm$renderHotbarItem(int slot, int x, int y, float tickDelta, PlayerEntity playerEntity, CallbackInfo ci) {
        if (mod.isDisableHotbarRender() || !mod.inGame()) {
            return;
        }

        try {
            ItemStack itemStack = playerEntity.inventory.main[slot];
            if (itemStack == null || !ZUtil.isWeapon(itemStack))
                return;

            TextRenderer textRenderer = this.client.textRenderer;
            ItemRendererAccessor itemRenderer = (ItemRendererAccessor) this.itemRenderer;

            try {
                zm$renderReloadGauge(itemRenderer, x, y, itemStack);
                if (!itemStack.isDamaged()) { //  || 1 < itemStack.count) {  // リロード中 or 2以上なら(ダメージ値が残像した時の対策)
                    zm$renderAmmoOverlay(textRenderer, itemRenderer, x, y, itemStack);
                }
                zm$renderTotalAmmoOverlay(textRenderer, itemRenderer, x, y, slot, itemStack);
                ci.cancel();

            } catch (Throwable e) {
                e.printStackTrace();
            }

        } catch (Exception ignored) {
            ci.cancel();
        }
    }

    private void zm$renderReloadGauge(ItemRendererAccessor itemRenderer, int x, int y, ItemStack itemStack) {
        float reloadProgress = (float) itemStack.getDamage() / itemStack.getMaxDamage();

        if (itemStack.isDamaged() && reloadProgress > 0.0F) {
            GlStateManager.disableDepthTest();
            GlStateManager.disableTexture();
            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(770, 771, 1, 0);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder2 = tessellator.getBuffer();
            itemRenderer.invokeRenderGuiQuad(bufferBuilder2, x, y + MathHelper.floor(16.0F * (1.0F - reloadProgress)), 16, MathHelper.ceil(16.0F * reloadProgress), 255, 255, 255, 127);
            GlStateManager.enableTexture();
            GlStateManager.enableDepthTest();
        }
    }

    private void zm$renderAmmoOverlay(TextRenderer renderer, ItemRendererAccessor itemRenderer, int x, int y, ItemStack itemStack) {
        int ammo = itemStack.count;
        Integer clipAmmo = ZUtil.getClipAmmoByItemStack(itemStack);

        if (clipAmmo != null) {
            // gauge
            float prog = Math.max(0f, Math.min((float) ammo / clipAmmo, 1f));
            int i = Math.round(14f * prog);
            int j = MathHelper.hsvToRgb(prog / 3.0F, 1.0F, 1.0F);

            GlStateManager.disableLighting();
            GlStateManager.disableDepthTest();
            GlStateManager.disableTexture();
            GlStateManager.disableAlphaTest();
            GlStateManager.disableBlend();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            itemRenderer.invokeRenderGuiQuad(bufferBuilder, x + 2 - 1, y + 14, 14, 1, 0, 0, 0, 255);
            itemRenderer.invokeRenderGuiQuad(bufferBuilder, x + 2 - 1, y + 14, i, 1, j >> 16, j >> 8 & 255, j & 255, 255);
            GlStateManager.enableBlend();
            GlStateManager.enableAlphaTest();
            GlStateManager.enableTexture();
            GlStateManager.enableLighting();
            GlStateManager.enableDepthTest();

            // text
            String string;
            if (itemStack.isDamaged()) {
                string = Formatting.DARK_RED + "##";
            } else {
                string = Util.coloredCountText(ammo, clipAmmo).asFormattedString();
            }
            GlStateManager.disableLighting();
            GlStateManager.disableDepthTest();
            GlStateManager.disableBlend();
            renderer.drawWithShadow(string, (float) (x + 19 - 2 - renderer.getStringWidth(string)), (float) (y + 7), 16777215);
            GlStateManager.enableLighting();
            GlStateManager.enableDepthTest();

        } else {
            itemRenderer.invokeRenderGuiItemOverlay(renderer, itemStack, x, y, null);
        }
    }

    private void zm$renderTotalAmmoOverlay(TextRenderer renderer, ItemRendererAccessor itemRenderer, int x, int y, int slot, ItemStack itemStack) {
        int totalAmmo = mod.getTotalAmmo(slot);
        Integer fullAmmo = ZUtil.getTotalAmmoByItemStack(itemStack);

        if (fullAmmo != null) {
            // gauge
            int total = Math.min(totalAmmo, fullAmmo);
            int i = Math.round(14F - (float) (fullAmmo - total) * 14F / (float) fullAmmo);
            int j = MathHelper.hsvToRgb(Math.max(0.0F, total / (float) fullAmmo) / 3.0F, 1.0F, 1.0F);

            GlStateManager.disableLighting();
            GlStateManager.disableDepthTest();
            GlStateManager.disableTexture();
            GlStateManager.disableAlphaTest();
            GlStateManager.disableBlend();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            itemRenderer.invokeRenderGuiQuad(bufferBuilder, x + 2 - 1, y + 1, 14, 1, 0, 0, 0, 255);
            itemRenderer.invokeRenderGuiQuad(bufferBuilder, x + 2 - 1, y + 1, i, 1, j >> 16, j >> 8 & 255, j & 255, 255);
            GlStateManager.enableBlend();
            GlStateManager.enableAlphaTest();
            GlStateManager.enableTexture();
            GlStateManager.enableLighting();
            GlStateManager.enableDepthTest();

            // text
            String string = Util.coloredCountText(totalAmmo, fullAmmo).asFormattedString();
            GlStateManager.disableLighting();
            GlStateManager.disableDepthTest();
            GlStateManager.disableBlend();
            renderer.drawWithShadow(string, (float) (x + 19 - 2 - renderer.getStringWidth(string)), (float) (y), 16777215);
            GlStateManager.enableLighting();
            GlStateManager.enableDepthTest();
        }
    }


    @Inject(
            method = "renderBossBar",
            at = @At("RETURN")
    )
    private void zm$renderBossBar(CallbackInfo ci) {
        if (!mod.inGame())
            return;

        boolean hasVanilla = net.minecraft.entity.boss.BossBar.name != null && net.minecraft.entity.boss.BossBar.framesToLive > 0;
        int bars = hasVanilla ? 1 : 0;
        int width = new Window(client).getWidth();
        int j = 182;

        DrawableHelper helper = (DrawableHelper) (Object) this;

        try {
            int offsetY, m;

            int count = mod.getZombieCount();
            if (0 < count) {
                offsetY = bars * (12 + 5 + 2);
                m = offsetY + 12;
                bars++;
                int total = mod.getAllZombieCount();
                float percent = total <= -1 ? 1 : Math.min((float) count / total, 1f);
                String label = Formatting.WHITE + "残りゾンビ: " + Formatting.GREEN + count;

                int k = width / 2 - j / 2;
                int l = (int) (percent * (float) (j + 1));
                helper.drawTexture(k, m, 0, 74, j, 5);
                helper.drawTexture(k, m, 0, 74, j, 5);

                if (l > 0)
                    helper.drawTexture(k, m, 0, 79, l, 5);

                getFontRenderer().drawWithShadow(label, (float) (width / 2 - this.getFontRenderer().getStringWidth(label) / 2), (float) (m - 10), 16777215);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.client.getTextureManager().bindTexture(DrawableHelper.GUI_ICONS_TEXTURE);
            }

            ZombiesModeMod.ReviveState reviveState = mod.getReviveState();
            if (reviveState != null) {
                offsetY = bars * (12 + 5 + 2);
                m = offsetY + 12;
                bars++;
//                float percent = 1f + (-reviveState.getCurrentTime() / Math.max(0.1f, reviveState.getTotalTime()));
                float percent = ((float) (System.currentTimeMillis() - reviveState.getStartTime())) / (reviveState.getTotalTime() * 1000);

                String label = reviveState.getMessage().asFormattedString();

                int k = width / 2 - j / 2;
                int l = (int) (percent * (float) (j + 1));
                helper.drawTexture(k, m, 0, 74, j, 5);
                helper.drawTexture(k, m, 0, 74, j, 5);

                if (l > 0)
                    helper.drawTexture(k, m, 0, 79, l, 5);

                getFontRenderer().drawWithShadow(label, (float) (width / 2 - this.getFontRenderer().getStringWidth(label) / 2), (float) (m - 10), 16777215);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.client.getTextureManager().bindTexture(DrawableHelper.GUI_ICONS_TEXTURE);

            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/InGameHud;showCrosshair()Z",
                    shift = At.Shift.BY,
                    by = -1
            )
    )
    private void mz$renderCritical(float tickDelta, CallbackInfo ci) {
        if (!mod.inGame() || !mod.isCriticalHit())
            return;

        Window window = new Window(client);
        int centerX = window.getWidth() / 2;
        int centerY = window.getHeight() / 2;

        float red = .5f, green = 1f, blue = 1f, alpha = 1f;
        int width = 3;
        int height = 1;
        int x = centerX - 10 - width;
        int y = centerY;

//        GlStateManager.enableBlend();
//        GlStateManager.enableAlphaTest();
        GlStateManager.disableTexture();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, VertexFormats.POSITION_COLOR);
        buffer.vertex(x, y, 0.0).color(red, green, blue, alpha).next();
        buffer.vertex(x, y + height, 0.0).color(red, green, blue, alpha).next();
        buffer.vertex(x + width, y + height, 0.0).color(red, green, blue, alpha).next();
        buffer.vertex(x + width, y, 0.0).color(red, green, blue, alpha).next();

        x = centerX + 10;
        buffer.vertex(x, y, 0.0).color(red, green, blue, alpha).next();
        buffer.vertex(x, y + height, 0.0).color(red, green, blue, alpha).next();
        buffer.vertex(x + width, y + height, 0.0).color(red, green, blue, alpha).next();
        buffer.vertex(x + width, y, 0.0).color(red, green, blue, alpha).next();

        width = 1;
        height = 3;
        x = centerX;
        y = centerY - 10 - height;
        buffer.vertex(x, y, 0.0).color(red, green, blue, alpha).next();
        buffer.vertex(x, y + height, 0.0).color(red, green, blue, alpha).next();
        buffer.vertex(x + width, y + height, 0.0).color(red, green, blue, alpha).next();
        buffer.vertex(x + width, y, 0.0).color(red, green, blue, alpha).next();

        y = centerY + 10;
        buffer.vertex(x, y, 0.0).color(red, green, blue, alpha).next();
        buffer.vertex(x, y + height, 0.0).color(red, green, blue, alpha).next();
        buffer.vertex(x + width, y + height, 0.0).color(red, green, blue, alpha).next();
        buffer.vertex(x + width, y, 0.0).color(red, green, blue, alpha).next();

        Tessellator.getInstance().draw();
        GlStateManager.enableTexture();
    }



    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    private void mz$renderHead(float tickDelta, CallbackInfo ci) {
        if (!mod.inGame())
            return;

        int combo = mod.getCombo();

        if (0 < overlayRemaining) {
            String strip = Formatting.strip(overlayMessage);
            if ((strip.isEmpty() && 0 < combo) || ZUtil.isIgnoreActionbarMessage(strip)) {
                overlayRemaining = 0;
            }
        }

        if (combo <= 0)
            return;

        if (overlayRemaining <= 0 || modifiedOverlayMessageLast.equals(overlayMessage)) {
            overlayRemaining = 40;
            overlayMessage = Formatting.AQUA + "HITS: " + Formatting.GOLD + Formatting.BOLD + combo;
            modifiedOverlayMessageLast = overlayMessage;
        }

    }


}
