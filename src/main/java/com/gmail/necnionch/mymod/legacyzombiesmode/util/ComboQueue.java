package com.gmail.necnionch.mymod.legacyzombiesmode.util;

import java.util.ArrayDeque;
import java.util.Queue;

public class ComboQueue {
    private final float delay;
    private final Queue<Entry> entries = new ArrayDeque<>();


    public ComboQueue(float delay) {
        this.delay = delay;
    }


    public void add() {
        entries.add(new Entry());
    }

    public void clear() {
        entries.clear();
    }

    public int combo() {
        long now = System.currentTimeMillis();
        Entry entry = entries.peek();
        while (entry != null && now - entry.getTime() > delay) {
            entries.poll();
            entry = entries.peek();
        }

        return entries.size();
    }

    public static class Entry {
        private final long time = System.currentTimeMillis();
        public Entry() {}

        public long getTime() {
            return time;
        }
    }

}
