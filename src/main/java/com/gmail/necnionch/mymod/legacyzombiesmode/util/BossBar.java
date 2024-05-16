package com.gmail.necnionch.mymod.legacyzombiesmode.util;

public class BossBar {

    private String label;
    private float percent;

    public BossBar(String label, float percent) {
        this.label = label;
        this.percent = percent;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setPercent(float percent) {
        this.percent = percent;
    }

    public float getPercent() {
        return percent;
    }

}
