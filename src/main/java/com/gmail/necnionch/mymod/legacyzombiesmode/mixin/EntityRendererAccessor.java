package com.gmail.necnionch.mymod.legacyzombiesmode.mixin;


import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityRenderer.class)
public interface EntityRendererAccessor {

    @Invoker("bindTexture")
    boolean invokeBindTexture(Entity entity);

}
