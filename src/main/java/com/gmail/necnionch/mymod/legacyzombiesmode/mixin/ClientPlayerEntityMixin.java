package com.gmail.necnionch.mymod.legacyzombiesmode.mixin;

import com.gmail.necnionch.mymod.legacyzombiesmode.ZombiesModeMod;
import com.google.common.collect.Lists;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.function.Consumer;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Shadow
    protected MinecraftClient client;

    @Inject(
            method = "sendChatMessage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void zm$sendChatMessage(String text, CallbackInfo ci) {
        if (text.startsWith("/myzombies") || text.startsWith("/myzm") || text.startsWith("/debugzombies")) {
            ci.cancel();
            try {
                ArrayList<String> args = Lists.newArrayList(text.split(" "));
                args.remove(0);

                Consumer<Text> sender = (m) -> client.inGameHud.getChatHud().addMessage(m);
                ZombiesModeMod.getInstance().executeDebugCommand(sender, args);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Inject(
            method = "setExperience",
            at = @At("RETURN")
    )
    private void zm$setExperience(float progress, int total, int level, CallbackInfo ci) {
        try {
            ZombiesModeMod.getInstance().onSetExperience(progress, total, level);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
