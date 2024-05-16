package com.gmail.necnionch.mymod.legacyzombiesmode.mixin;

import com.gmail.necnionch.mymod.legacyzombiesmode.ZombiesModeMod;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow private ClientWorld world;

    @Inject(
            method = "onScoreboardObjectiveUpdate",
            at = @At("RETURN")
    )
    private void mz$onScoreboardObjectiveUpdate(ScoreboardObjectiveUpdateS2CPacket packet, CallbackInfo ci) {
        if (packet.getMode() == 1) {  // remove objective
            try {
                ZombiesModeMod.getInstance().onScoreboardObjectiveHide(packet.getName());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Inject(
            method = "onScoreboardDisplay",
            at = @At("RETURN")
    )
    private void mz$onScoreboardDisplay(ScoreboardDisplayS2CPacket packet, CallbackInfo ci) {
        if (this.world == null)
            return;

        try {
            if (packet.getSlot() == 1) {  // sidebar
                if (packet.getName().isEmpty()) {
                    ZombiesModeMod.getInstance().onScoreboardObjectiveHide();
                } else {
                    ScoreboardObjective objective = this.world.getScoreboard().getNullableObjective(packet.getName());
                    if (objective == null)
                        return;
                    ZombiesModeMod.getInstance().onScoreboardObjectiveShow(objective);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Inject(
            method = "onScoreboardPlayerUpdate",
            at = @At("RETURN")
    )
    private void mz$onScoreboardPlayerUpdate(ScoreboardPlayerUpdateS2CPacket packet, CallbackInfo ci) {
        if (packet.getType() != ScoreboardPlayerUpdateS2CPacket.UpdateType.CHANGE)
            return;

        try {
            ZombiesModeMod.getInstance().onScoreboardObjectiveLine(packet.getObjectiveName(), packet.getScore(), packet.getPlayerName());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    @Inject(
            method = "onTitle",
            at = @At("RETURN")
    )
    private void mz$onTitle(TitleS2CPacket packet, CallbackInfo ci) {
        if (packet.getText() == null)
            return;

        try {
            ZombiesModeMod.getInstance().onTitle(packet.getAction(), packet.getText());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Inject(
            method = "onTeam",
            at = @At("RETURN")
    )
    private void mz$onTeam(TeamS2CPacket packet, CallbackInfo ci) {
        int mode = packet.getMode();

        if (mode == 0) { // create

        } else if (mode == 2) { // modify

        } else if (mode == 4) {  // remove players

        } else if (mode == 1) { // remove team
        }


        if (mode == 0 || mode == 2) {
            Team team = this.world.getScoreboard().getTeam(packet.getTeamName());
            if (team != null) {
                try {
                    ZombiesModeMod.getInstance().onTeam(team);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }

    }



    @Inject(
            method = "onChatMessage",
            at = @At("RETURN")
    )
    private void mz$onChatMessage(ChatMessageS2CPacket packet, CallbackInfo ci) {
        Text message = packet.getMessage();
        if (packet.getType() == 2) {
            try {
                ZombiesModeMod.getInstance().onActionbarMessage(message);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

    }


}
