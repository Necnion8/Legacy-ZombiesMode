package com.gmail.necnionch.mymod.legacyzombiesmode.util;

import com.gmail.necnionch.mymod.legacyzombiesmode.ZombiesModeMod;
import net.legacyfabric.fabric.api.logger.v1.Logger;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ZUtil {
    private static final Logger log = Logger.get("ZombiesModeMod", "ZUtil");
    public static final String SCOREBOARD_TITLE = "ZOMBIES";
    public static final String SCOREBOARD_FOOTER = "www.hypixel.net";
    public static final Pattern[] SCOREBOARD_ROUND = new Pattern[] {
            Pattern.compile("^Round", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^ラウンド", Pattern.CASE_INSENSITIVE),
    };
    public static final Pattern[] SCOREBOARD_ZOMBIES = new Pattern[] {
            Pattern.compile("^Zombies Left", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^残りゾンビ", Pattern.CASE_INSENSITIVE),
    };
    public static final Pattern ENDS_INTEGER = Pattern.compile("(\\d+)$");
    public static final Pattern[] WEAPON_CLIP_AMMO_LORE_LINE = new Pattern[] {
            Pattern.compile("clip ammo: \\d+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("装填数: \\d+", Pattern.CASE_INSENSITIVE),
    };
    public static final Pattern[] WEAPON_TOTAL_AMMO_LORE_LINE = new Pattern[] {
            Pattern.compile("ammo: \\d+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("弾数: \\d+", Pattern.CASE_INSENSITIVE),
    };
    public static final Pattern[] CHAT_GOLD = new Pattern[] {
            Pattern.compile("^\\+(\\d+) ゴールド *(\\(クリティカル)?"),
    };
    public static final Pattern[] CHAT_MAX_AMMO = new Pattern[] {
            Pattern.compile("Ammo Supply をチームマシーンから発動しました"),
            Pattern.compile("マックスアモ を発動させました"),
    };
    public static final Pattern[] ACTIONBAR_REVIVE_TIME = new Pattern[] {
            Pattern.compile("を復活させています - (\\d+\\.\\d+)s"),
    };
    public static final Pattern[] ACTIONBAR_DOWN_TIME = new Pattern[] {
            Pattern.compile("があなたを復活させています - (\\d+\\.\\d+)s"),
    };
    public static final Pattern[] ACTIONBAR_IGNORES = new Pattern[] {
            Pattern.compile("^スニークを押し続けて修理する。$"),
            Pattern.compile("^右クリックして購入します。$"),
            Pattern.compile("^リロード中$"),
    };
    public static final Pattern[] CHAT_IGNORES = new Pattern[] {
            Pattern.compile("^窓を修理しています。スニークを押し続けて修理を続けられます。$"),
            Pattern.compile("^この窓を完全に修理しました！$"),
            Pattern.compile("^修理を中断しました。スニークを押し続けて修理を続けられます！$"),
            Pattern.compile("^修理を中断しました。修理するには窓の範囲に居る必要があります！$"),
//            Pattern.compile("^修理を中断しました。周辺に敵がいます！$"),
            Pattern.compile("^修理を中断しました。付近に敵がいます！$"),
            Pattern.compile("^敵が近くにいる間は窓を修理できません！$"),
    };
    public static final int SCOREBOARD_ROUND_LINE = 13;
    public static final int SCOREBOARD_ZOMBIES_LINE = 12;
    public static final int SCOREBOARD_KILLED_LINE = 5;

    @Nullable
    public static List<String> getLore(NbtCompound nbt) {
        if (nbt.contains("display", 10)) {
            NbtCompound nbtCompound = nbt.getCompound("display");
            if (nbtCompound.getType("Lore") == 9) {
                ArrayList<String> lines = new ArrayList<>();
                NbtList lore = nbtCompound.getList("Lore", 8);
                for (int i = 0; i < lore.size(); i++) {
                    lines.add(lore.getString(i));
                }
                return lines;
            }
        }
        return null;
    }

    @Nullable
    public static List<String> getLore(ItemStack itemStack) {
        return getLore(itemStack.getNbt());
    }

    public static Optional<ZWeaponType> getWeaponType(@Nullable NbtCompound nbt) {
        if (nbt == null || !nbt.contains("ExtraAttributes", 10))
            return Optional.empty();

        NbtCompound extra = nbt.getCompound("ExtraAttributes");
        if (!extra.contains("weapon", 8))
            return Optional.empty();

        String id = extra.getString("weapon");
        ZWeaponType type = ZWeaponType.getTypeById(id);
        if (ZWeaponType.UNKNOWN.equals(type)) {
//            log.warn("Unknown weapon id: " + id);
            ZombiesModeMod.getInstance().onUnknownWeapon(id);
        }
        return Optional.of(type);
    }

    public static Optional<ZWeaponType> getWeaponType(@Nullable ItemStack itemStack) {
        return Optional.ofNullable(itemStack)
                .flatMap(is -> getWeaponType(is.getNbt()));
    }

    public static boolean isWeapon(@Nullable NbtCompound nbt) {
        return nbt != null && nbt.contains("ExtraAttributes", 10);
    }

    public static boolean isWeapon(ItemStack itemStack) {
        return isWeapon(itemStack.getNbt());
    }


    @Nullable
    public static Integer getClipAmmoByItemStack(ItemStack itemStack) {
        List<String> lore = getLore(itemStack);
        if (lore == null)
            return null;

        for (String line : lore) {
            String strippedLine = Formatting.strip(line);
            Optional<Integer> matcher = Stream.of(WEAPON_CLIP_AMMO_LORE_LINE)
                    .filter(p -> p.matcher(strippedLine).find())
                    .map(m -> ENDS_INTEGER.matcher(strippedLine))
                    .filter(Matcher::find)
                    .map(m -> Integer.parseInt(m.group(1)))
                    .findFirst();

            if (matcher.isPresent())
                return matcher.get();
        }

        return null;
    }

    @Nullable
    public static Integer getTotalAmmoByItemStack(ItemStack itemStack) {
        List<String> lore = getLore(itemStack);
        if (lore == null)
            return null;

        for (String line : lore) {
            String strippedLine = Formatting.strip(line);
            Optional<Integer> matcher = Stream.of(WEAPON_TOTAL_AMMO_LORE_LINE)
                    .filter(p -> p.matcher(strippedLine).find())
                    .map(m -> ENDS_INTEGER.matcher(strippedLine))
                    .filter(Matcher::find)
                    .map(m -> Integer.parseInt(m.group(1)))
                    .findFirst();

            if (matcher.isPresent())
                return matcher.get();
        }

        return null;
    }


    public static OptionalInt getRoundByString(String string) {
        String strippedLine = Formatting.strip(string);
        return Stream.of(SCOREBOARD_ROUND)
                .filter(p -> p.matcher(strippedLine).find())
                .map(m -> ENDS_INTEGER.matcher(strippedLine))
                .filter(Matcher::find)
                .mapToInt(m -> Integer.parseInt(m.group(1)))
                .findFirst();
    }

    public static OptionalInt getZombieCountByString(String string) {
        String strippedLine = Formatting.strip(string);
        return Stream.of(SCOREBOARD_ZOMBIES)
                .filter(p -> p.matcher(strippedLine).find())
                .map(m -> ENDS_INTEGER.matcher(strippedLine))
                .filter(Matcher::find)
                .mapToInt(m -> Integer.parseInt(m.group(1)))
                .findFirst();
    }

    public static Optional<AddGold> getAddedGoldByString(String string) {
        String strippedLine = Formatting.strip(string);
        return Stream.of(CHAT_GOLD)
                .map(p -> p.matcher(strippedLine))
                .filter(Matcher::find)
                .findFirst()
                .map(m -> new AddGold(Integer.parseInt(m.group(1)), m.group(2) != null));
    }

    public static boolean isMaxAmmoMessage(String string) {
        String strippedLine = Formatting.strip(string);
        return Stream.of(CHAT_MAX_AMMO)
                .map(p -> p.matcher(strippedLine))
                .anyMatch(Matcher::find);
    }

    public static OptionalDouble getReviveTimeByString(String string) {
        String strippedLine = Formatting.strip(string);
        return Stream.of(ACTIONBAR_REVIVE_TIME)
                .map(p -> p.matcher(strippedLine))
                .filter(Matcher::find)
                .mapToDouble(m -> Double.parseDouble(m.group(1)))
                .findFirst();
    }

    public static OptionalDouble getDownTimeByString(String string) {
        String strippedLine = Formatting.strip(string);
        return Stream.of(ACTIONBAR_DOWN_TIME)
                .map(p -> p.matcher(strippedLine))
                .filter(Matcher::find)
                .mapToDouble(m -> Double.parseDouble(m.group(1)))
                .findFirst();
    }

    public static boolean isIgnoreActionbarMessage(String string) {
        String strippedLine = Formatting.strip(string);
        return Stream.of(ACTIONBAR_IGNORES)
                .map(p -> p.matcher(strippedLine))
                .anyMatch(Matcher::find);
    }

    public static boolean isIgnoreChatMessage(String string) {
        String strippedLine = Formatting.strip(string);
        return Stream.of(CHAT_IGNORES)
                .map(p -> p.matcher(strippedLine))
                .anyMatch(Matcher::find);
    }


    public static class AddGold {

        private final int gold;
        private final boolean critical;

        public AddGold(int gold, boolean critical) {
            this.gold = gold;
            this.critical = critical;
        }

        public int getGold() {
            return gold;
        }

        public boolean isCritical() {
            return critical;
        }
    }



}
