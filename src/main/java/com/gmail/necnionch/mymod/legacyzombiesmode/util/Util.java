package com.gmail.necnionch.mymod.legacyzombiesmode.util;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.Display;

import java.util.Arrays;
import java.util.stream.Stream;

public class Util {
    public static void setWindowLocationToTest() {
        try {
            Display.setLocation(100, -700);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static Text text(String text, Formatting... formatting) {
        LiteralText result = new LiteralText(text);
        Style style = result.getStyle();

        if (Arrays.asList((formatting)).contains(Formatting.RESET)) {
            style.setFormatting(Formatting.RESET);
            return result;
        }

        Stream.of(formatting).filter(Formatting::isColor).findAny().ifPresent(style::setFormatting);

        for (Formatting format : formatting) {
            if (format.isColor())
                continue;

            switch (format) {
                case OBFUSCATED: {
                    style.setObfuscated(true);
                    break;
                }
                case BOLD: {
                    style.setBold(true);
                    break;
                }
                case STRIKETHROUGH: {
                    style.setStrikethrough(true);
                    break;
                }
                case UNDERLINE: {
                    style.setUnderline(true);
                    break;
                }
                case ITALIC: {
                    style.setItalic(true);
                    break;
                }
            }
        }
        return result;
    }

    public static Text coloredCountText(int value, @Nullable Integer maxValue) {
        LiteralText text = new LiteralText(String.valueOf(value));

        if (value <= 0) {
            text.getStyle().setFormatting(Formatting.RED);
            return text;
        } else if (maxValue == null) {
            return text;
        }

        if (value == maxValue) {
            text.getStyle().setFormatting(Formatting.GREEN);
        } else if ((float) value / maxValue <= 0.2 || 1 == value) {
            text.getStyle().setFormatting(Formatting.GOLD);
        }

        return text;
    }

}
