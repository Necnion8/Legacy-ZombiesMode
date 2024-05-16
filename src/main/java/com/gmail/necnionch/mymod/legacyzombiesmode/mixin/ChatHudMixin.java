package com.gmail.necnionch.mymod.legacyzombiesmode.mixin;

import com.gmail.necnionch.mymod.legacyzombiesmode.ZombiesModeMod;
import com.gmail.necnionch.mymod.legacyzombiesmode.util.ZUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static com.gmail.necnionch.mymod.legacyzombiesmode.util.Util.text;

@Mixin(value = ChatHud.class, priority = 100)
public class ChatHudMixin {
    private final ZombiesModeMod mod = ZombiesModeMod.getInstance();
    @Shadow @Final private List<ChatHudLine> visibleMessages;
    @Shadow @Final private MinecraftClient client;
    private int stackedCount;
    private int stackedGold;
    private boolean lastGoldMessage;


    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/ChatHud;fill(IIIII)V"
            ),
            index = 4
    )
    private int mz$renderOverrideShadow(int color) {
        return (mod.inGame()) ? (color >> 24) / 2 << 24 : color;
    }


    @Inject(
            method = "addMessage(Lnet/minecraft/text/Text;I)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void mz$addMessage(Text message, int messageId, CallbackInfo ci) {
        if (!mod.inGame() || mod.isDisableChatMixin()) {
            lastGoldMessage = false;
            stackedGold = 0;
            stackedCount = 0;
            return;
        }

        String unformattedString = message.asUnformattedString();
        try {
            if (ZUtil.isIgnoreChatMessage(unformattedString)) {
                ci.cancel();
                return;
            }

            ZUtil.AddGold added = ZUtil.getAddedGoldByString(unformattedString).orElse(null);
            if (added != null) {
                mz$processGoldMessage(messageId, ci, added);
            } else {
                lastGoldMessage = false;
                stackedGold = 0;
                stackedCount = 0;
            }

            if (ZUtil.isMaxAmmoMessage(unformattedString))
                mod.onMaxAmmo();


        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void mz$processGoldMessage(int messageId, CallbackInfo ci, ZUtil.AddGold added) {
        mod.onAddedGold(added);

        stackedGold += added.getGold();
        stackedCount++;

        if (lastGoldMessage && !this.visibleMessages.isEmpty()) {
            Text stackedText = text("[x" + stackedCount + "] ", Formatting.WHITE);
            stackedText.append(text ("+" + stackedGold + " ゴールド", Formatting.GOLD));
            stackedText.append(text(" (+" + added.getGold() + ")", Formatting.GOLD));
            ChatHudLine line = new ChatHudLine(this.client.inGameHud.getTicks(), stackedText, messageId);
            this.visibleMessages.set(0, line);
            ci.cancel();
        }

        lastGoldMessage = true;

    }


}
