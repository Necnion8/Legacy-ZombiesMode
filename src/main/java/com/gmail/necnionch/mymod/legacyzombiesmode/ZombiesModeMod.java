package com.gmail.necnionch.mymod.legacyzombiesmode;

import com.gmail.necnionch.mymod.legacyzombiesmode.util.ComboQueue;
import com.gmail.necnionch.mymod.legacyzombiesmode.util.ZUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.fabricmc.api.ClientModInitializer;
import net.legacyfabric.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.legacyfabric.fabric.api.logger.v1.Logger;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

import static com.gmail.necnionch.mymod.legacyzombiesmode.util.Util.text;

public class ZombiesModeMod implements ClientModInitializer {
    public static final float PLAYER_INVISIBLE_ALPHA = 0.1f;
    public static final float PLAYER_INVISIBLE_DISTANCE = 2f;
    private final Logger log = Logger.get("ZombiesModeMod");
    private final MinecraftClient client = MinecraftClient.getInstance();
    private static ZombiesModeMod INSTANCE;
    private int selectedSlot = 0;
    private @Nullable ScoreboardObjective shownScoreboard;
    private final int[] hotBarSlotsTotalAmmo = new int[9];
    private boolean ignoreOnExperienceUpdate = false;
    private boolean nextNewRoundZombies = false;
    private boolean forceInGame = false;
    private boolean disableHotbarRender = false;
    private boolean disableChatMixin = false;
    private boolean disablePlayerVisibility = false;

    private int currentRound = -1;
    private int allZombieCount = -1;
    private int zombieCount;
    private long criticalHitTick;
    private final Map<String, Integer> scoresByName = Maps.newHashMap();
    private @Nullable ZombiesModeMod.ReviveState reviveState;
    private final ComboQueue combos = new ComboQueue(1000);

    private final Set<String> unknownNewIds = Sets.newHashSet();


    /**
     * ゴールド変動をサイドボードに
     */


    public static ZombiesModeMod getInstance() {
        return INSTANCE;
    }

    public boolean isEnableForceInGame() {
        return forceInGame;
    }

    public boolean isDisableHotbarRender() {
        return disableHotbarRender;
    }

    public boolean isDisableChatMixin() {
        return disableChatMixin;
    }

    public boolean isDisablePlayerVisibility() {
        return disablePlayerVisibility;
    }

    public void executeDebugCommand(Consumer<Text> sender, List<String> args) {
        if (args.size() == 1 && "dumpitems".equalsIgnoreCase(args.get(0))) {
            sender.accept(text("インベントリにあるアイテムを書き出します...", Formatting.GRAY));
            boolean result;
            try {
                result = dumpInventoryItems();
            } catch (Exception e) {
                e.printStackTrace();
                sender.accept(text("失敗しました: ", Formatting.RED).append(text(e.getMessage(), Formatting.DARK_RED)));
                return;
            }

            if (result) {
                sender.accept(text("完了", Formatting.GREEN));
            } else {
                sender.accept(text("失敗", Formatting.RED));
            }

        } else if (1 <= args.size() && "forceingame".equalsIgnoreCase(args.get(0))) {
            forceInGame = (args.size() == 1) ? !forceInGame : args.get(1).equalsIgnoreCase("true");
            sender.accept(text("forceInGame = ", Formatting.GOLD)
                    .append(text(forceInGame ? "true" : "false", forceInGame ? Formatting.GREEN : Formatting.RED)));

        } else if (1 <= args.size() && "disablehotbarrender".equalsIgnoreCase(args.get(0))) {
            disableHotbarRender = (args.size() == 1) ? !disableHotbarRender : args.get(1).equalsIgnoreCase("true");
            sender.accept(text("disableHotbarRender = ", Formatting.GOLD)
                    .append(text(disableHotbarRender ? "true" : "false", disableHotbarRender ? Formatting.GREEN : Formatting.RED)));

        } else if (1 <= args.size() && "disableplayervisibility".equalsIgnoreCase(args.get(0))) {
            disablePlayerVisibility = (args.size() == 1) ? !disablePlayerVisibility : args.get(1).equalsIgnoreCase("true");
            sender.accept(text("disablePlayerVisibility = ", Formatting.GOLD)
                    .append(text(disablePlayerVisibility ? "true" : "false", disablePlayerVisibility ? Formatting.GREEN : Formatting.RED)));

        } else if (1 <= args.size() && "disablechatmixin".equalsIgnoreCase(args.get(0))) {
            disableChatMixin = (args.size() == 1) ? !disableChatMixin : args.get(1).equalsIgnoreCase("true");
            sender.accept(text("disableChatMixin = ", Formatting.GOLD)
                    .append(text(disableChatMixin ? "true" : "false", disableChatMixin ? Formatting.GREEN : Formatting.RED)));

        } else if (1 == args.size() && "test".equalsIgnoreCase(args.get(0))) {
            Scoreboard scoreboard = client.player.getScoreboard();
            for (Team team : scoreboard.getTeams()) {
                sender.accept(text("Team: " + team.getName(), Formatting.GOLD));
                sender.accept(text("  prefix: " + team.getPrefix()));
                sender.accept(text("  suffix: " + team.getSuffix()));
                sender.accept(text("  display: " + team.getDisplayName()));
                sender.accept(text("  formatting: " + Optional.ofNullable(team.getFormatting()).map(Enum::name).orElse(null)));
                sender.accept(text("  players: " + String.join("|", team.getPlayerList())));
                log.info("playerList");
                for (String s : team.getPlayerList()) {
                    log.info(Arrays.toString(s.getBytes(StandardCharsets.UTF_8)));
                }
            }

            for (ScoreboardObjective objective : scoreboard.getObjectives()) {
                sender.accept(text("Objective: " + objective.getName()));
                sender.accept(text("  display: " + objective.getDisplayName()));
                log.info("playerScores");
                for (ScoreboardPlayerScore score : scoreboard.getAllPlayerScores(objective)) {
                    sender.accept(text("  - playerScore"));
                    sender.accept(text("    score: " + score.getScore()));
                    sender.accept(text("    player: " + score.getPlayerName()));
                    log.info(Arrays.toString(score.getPlayerName().getBytes(StandardCharsets.UTF_8)));
                }
            }

        } else if (1 == args.size() && "modequit".equalsIgnoreCase(args.get(0))) {
            onQuitGame();

        } else if (1 == args.size() && "modejoin".equalsIgnoreCase(args.get(0))) {
            onJoinGame();

        } else {
            sender.accept(text("/myZm ", Formatting.GRAY)
                    .append(text("<", Formatting.GRAY))
                    .append(text("dumpitems", Formatting.WHITE))
                    .append(text("/", Formatting.GRAY))
                    .append(text("forceingame", Formatting.WHITE))
                    .append(text("/", Formatting.GRAY))
                    .append(text("disablehotbarrender", Formatting.WHITE))
                    .append(text("/", Formatting.GRAY))
                    .append(text("disableplayervisibility", Formatting.WHITE))
                    .append(text("/", Formatting.GRAY))
                    .append(text("disablechatmixin", Formatting.WHITE))
                    .append(text(">", Formatting.GRAY))
            );
        }

    }


    @Override
    public void onInitializeClient() {
        INSTANCE = this;
//        Util.setWindowLocationToTest();

        ClientTickEvents.END_CLIENT_TICK.register(c2 -> {
            if (0 < criticalHitTick)
                criticalHitTick--;

            Optional.of(client).map(c -> c.player).map(p -> p.inventory).ifPresent(inv -> {
                if (this.selectedSlot != inv.selectedSlot) {
                    this.selectedSlot = inv.selectedSlot;
                    onHeldItemChange(this.selectedSlot);
                }

                ItemStack itemStack = inv.main[selectedSlot];
                ZUtil.getWeaponType(itemStack);
            });

        });

        File idsFile = new File(client.runDirectory, "zombies_unknown_ids.txt");
        if (idsFile.isFile()) {
            try (FileReader reader = new FileReader(idsFile);
                 BufferedReader br = new BufferedReader(reader)) {
                String line = br.readLine();
                if (line != null && !line.isEmpty()) {
                    String[] split = line.split(",");
                    Collections.addAll(unknownNewIds, split);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    public boolean dumpInventoryItems() {
        if (client.player == null) {
            return false;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("container:\n");
        for (ItemStack itemStack : client.player.inventory.main) {
            if (itemStack == null) {
                sb.append("- null\n");
            } else {
                sb.append("- ").append(Item.REGISTRY.getIdentifier(itemStack.getItem()));
                if (itemStack.hasNbt()) {
                    sb.append(itemStack.getNbt().toString());
                }
                sb.append("\n");
            }
        }

        sb.append("\narmors:\n");
        for (ItemStack itemStack : client.player.inventory.armor) {
            if (itemStack == null) {
                sb.append("- null\n");
            } else {
                sb.append("- ").append(Item.REGISTRY.getIdentifier(itemStack.getItem()));
                if (itemStack.hasNbt()) {
                    sb.append(itemStack.getNbt().toString());
                }
                sb.append("\n");
            }
        }

        String now = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        try (PrintWriter writer = new PrintWriter(new File(client.runDirectory, "zombiesmode_" + now + "_dumpitems.txt"), "UTF-8")) {
            writer.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean inGame() {
        return shownScoreboard != null || forceInGame;
    }

    public int getTotalAmmo(int slot) {
        return hotBarSlotsTotalAmmo[slot];
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public int getAllZombieCount() {
        return allZombieCount;
    }

    public int getZombieCount() {
        return zombieCount;
    }

    public boolean isCriticalHit() {
        return 0 < criticalHitTick;
    }

    public @Nullable ReviveState getReviveState() {
        return reviveState;
    }

    public int getCombo() {
        return combos.combo();
    }

    private StringBuilder getStackPreview(@Nullable ItemStack itemStack) {
        StringBuilder sb = new StringBuilder();
        if (itemStack == null) {
            sb.append("null");
        } else {
            sb.append(Item.REGISTRY.getIdentifier(itemStack.getItem()));
            if (itemStack.hasNbt()) {
                sb.append(itemStack.getNbt().toString().replace(",", "\\,"));
            }
        }
        return sb;
    }


    public void onSetExperience(float progress, int total, int level) {
//        log.info("set experience, progress=" + progress + ", total=" + total + ", level=" + level);

        if (inGame() && client.player != null) {
            ItemStack handItemStack = client.player.inventory.getMainHandStack();
            if (handItemStack == null || !ZUtil.isWeapon(handItemStack))
                return;

            if (progress == 0) {  // 持ちかえた時に、一度 progress が 0 になるのを利用する
                ignoreOnExperienceUpdate = false;
            } else if (ignoreOnExperienceUpdate) {
                return;
            }

            hotBarSlotsTotalAmmo[client.player.inventory.selectedSlot] = level;  // total ammo
//            log.info("set ammo: " + level);
        }

    }

    public void onHeldItemChange(int slot) {
//        log.info("select slot: " + slot);
        ignoreOnExperienceUpdate = true;
    }

    public void onScoreboardObjectiveShow(ScoreboardObjective objective) {
        if (ZUtil.SCOREBOARD_TITLE.equals(Formatting.strip(objective.getDisplayName()))) {
            boolean joinNow = shownScoreboard == null;
            shownScoreboard = objective;

//            log.info("on show scoreboard (mapping score name to index)");
            for (ScoreboardPlayerScore score : objective.getScoreboard().getAllPlayerScores(objective)) {
//                log.warn("score: " + score.getScore() + " -> " + Arrays.toString(score.getPlayerName().getBytes(StandardCharsets.UTF_8)));
                scoresByName.put(score.getPlayerName(), score.getScore());
            }

            if (joinNow)
                onJoinGame();

        } else {  // show other sideboard
            onQuitGame();
        }
    }

    public void onScoreboardObjectiveHide(String objective) {
        if (shownScoreboard != null && shownScoreboard.getName().equals(objective)) {
            shownScoreboard = null;
            onQuitGame();
        }
    }

    public void onScoreboardObjectiveHide() {
        if (shownScoreboard != null) {
            shownScoreboard = null;
            onQuitGame();
        }
    }

    public void onScoreboardObjectiveLine(String objective, int score, String playerName) {
        if (shownScoreboard == null || !shownScoreboard.getName().equals(objective))
            return;

        scoresByName.put(playerName, score);
    }

    private void onJoinGame() {
        log.info("Join Zombies detect");
    }

    private void onQuitGame() {
        log.info("Quit Zombies detect");

        shownScoreboard = null;
        Arrays.fill(hotBarSlotsTotalAmmo, 0);
        ignoreOnExperienceUpdate = false;
        nextNewRoundZombies = false;
        currentRound = -1;
        allZombieCount = -1;
        zombieCount = 0;
        scoresByName.clear();
        reviveState = null;
        combos.clear();
    }

    private void onRound(int round) {
        this.currentRound = round;
        nextNewRoundZombies = true;
        log.info("onRound -> " + round);
    }


    public void onUnknownWeapon(String id) {
        if (unknownNewIds.add(id)) {
            try (PrintWriter writer = new PrintWriter(new File(client.runDirectory, "zombies_unknown_ids.txt"))) {
                writer.write(String.join(",", unknownNewIds));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onTitle(TitleS2CPacket.Action action, Text text) {
//        if (!TitleS2CPacket.Action.TITLE.equals(action))
//            return;
//        ZUtil.getRoundByTitle(text.asUnformattedString()).ifPresent(this::onRound);

//        log.info("onTitle");
//        log.info("  action: " + action.name());
//        log.info("  text: " + text.asUnformattedString());
    }

    public void onAddedGold(ZUtil.AddGold added) {
//        log.info("added gold " + added.getGold() + " (crit: " + added.isCritical() + ")");
        if (added.isCritical())
            criticalHitTick = 2;

        combos.add();
    }

    public void onTeam(Team team) {
        for (Map.Entry<String, Integer> e : scoresByName.entrySet()) {
            String name = e.getKey();
            Integer score = e.getValue();

            if (team.getPlayerList().contains(name)) {
                String prefix = team.getPrefix();
                String suffix = team.getSuffix();
//                log.info("onTeam, score: " + score + ", prefix: " + prefix + ", suffix: " + suffix);
                String text = prefix + suffix;

                if (score == ZUtil.SCOREBOARD_ROUND_LINE) {  // round or gameover
                    int round = ZUtil.getRoundByString(text).orElse(-1);
                    if (round != -1 && currentRound != round) {
                        onRound(round);
                    }
                } else if (score == ZUtil.SCOREBOARD_ZOMBIES_LINE) {  // remaining zombies
                    if (nextNewRoundZombies) {
                        nextNewRoundZombies = false;
                        allZombieCount = ZUtil.getZombieCountByString(text).orElse(-1);
                    }
                    zombieCount = ZUtil.getZombieCountByString(text).orElse(0);

                }

                return;
            }
        }
    }

    public void onMaxAmmo() {
        if (inGame() && client.player != null) {
            for (int i = 0; i < 9; i++) {
                ItemStack itemStack = client.player.inventory.main[i];
                if (itemStack != null) {
                    Integer total = ZUtil.getTotalAmmoByItemStack(itemStack);
                    if (total != null) {
                        hotBarSlotsTotalAmmo[i] = total;
                    }
                }
            }
        }
    }

    public void onActionbarMessage(Text message) {
        String unformatted = message.asUnformattedString();

        OptionalDouble reviveTime = ZUtil.getReviveTimeByString(unformatted);
        if (reviveTime.isPresent()) {
            float time = (float) reviveTime.getAsDouble();
            if (reviveState == null || reviveState.getType() != 0)
                reviveState = new ReviveState(0, message, time, time, System.currentTimeMillis());
            reviveState.setCurrentTime(time);
            reviveState.setMessage(message);
            return;
        }

        OptionalDouble downTime = ZUtil.getDownTimeByString(unformatted);
        if (downTime.isPresent()) {
            float time = (float) downTime.getAsDouble();
            if (reviveState == null || reviveState.getType() != 1)
                reviveState = new ReviveState(1, message, time, time, System.currentTimeMillis());
            reviveState.setCurrentTime(time);
            reviveState.setMessage(message);
            return;
        }

        reviveState = null;
    }


    public static class ReviveState {

        private final int type;  // revive = 0, down = 1
        private final long startTime;
        private Text message;
        private final float totalTime;
        private float currentTime;

        public ReviveState(int type, Text message, float totalTime, float currentTime, long startTime) {
            this.type = type;
            this.message = message;
            this.totalTime = totalTime;
            this.currentTime = currentTime;
            this.startTime = startTime;
        }

        public ReviveState(int type, Text message, float totalTime, float currentTime) {
            this(type, message, totalTime, currentTime, System.currentTimeMillis());
        }

        public int getType() {
            return type;
        }

        public Text getMessage() {
            return message;
        }

        public float getTotalTime() {
            return totalTime;
        }

        public float getCurrentTime() {
            return currentTime;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setCurrentTime(float currentTime) {
            this.currentTime = currentTime;
        }

        public void setMessage(Text message) {
            this.message = message;
        }
    }

}
