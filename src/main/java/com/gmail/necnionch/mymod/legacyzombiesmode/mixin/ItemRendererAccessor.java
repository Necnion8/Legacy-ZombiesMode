package com.gmail.necnionch.mymod.legacyzombiesmode.mixin;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemRenderer.class)
public interface ItemRendererAccessor {

    @Invoker("renderGuiQuad")
    void invokeRenderGuiQuad(BufferBuilder buffer, int x, int y, int width, int height, int red, int green, int blue, int alpha);

    @Invoker("renderGuiItemOverlay")
    void invokeRenderGuiItemOverlay(TextRenderer renderer, ItemStack stack, int x, int y, String countLabel);

}
