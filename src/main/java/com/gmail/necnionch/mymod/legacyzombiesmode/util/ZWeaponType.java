package com.gmail.necnionch.mymod.legacyzombiesmode.util;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public enum ZWeaponType {
    PRACTICE_GUN("Practice Gun"),
    KNIFE("Knife"),
    PISTOL("Pistol"),
    RIFLE("Rifle"),
    SHOTGUN("Shotgun"),
    SNIPER("Sniper"),
    BLOW_DART("Blow Dart"),
    ROCKET_LAUNCHER("Rocket Launcher"),
    GOLD_DIGGER("Gold Digger"),
    FLAMETHROWER("Flamethrower"),
    ELDER_GUN("Elder Gun"),
    ZOMBIE_SOAKER("Zombie Soaker"),
    ZOMBIE_ZAPPER("Zombie Zapper"),
    PUNCHER("The Puncher"),

    UNKNOWN("");

    private final String id;

    ZWeaponType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static @NotNull ZWeaponType getTypeById(String id) {
        return Arrays.stream(ZWeaponType.values())
                .filter(t -> t.id.equals(id))
                .findAny()
                .orElse(UNKNOWN);
    }

}
