package com.internship.innowise.task3;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

class Factory {
    private final Map<PartType, Integer> parts = new EnumMap<>(PartType.class);
    private final Random random = new Random();
    private final Object lock = new Object();

    public Factory() {
        for (PartType type : PartType.values()) {
            parts.put(type, 0);
        }
    }

    public void produceParts() {
        synchronized (lock) {
            for (PartType type : PartType.values()) {
                parts.put(type, 0);
            }

            for (int i = 0; i < 10; i++) {
                PartType part = PartType.values()[random.nextInt(PartType.values().length)];
                parts.put(part, parts.get(part) + 1);
            }
            System.out.println("Factory produced: " + parts);
        }
    }

    public Map<PartType, Integer> takeParts(Map<PartType, Integer> needed, int maxTake) {
        synchronized (lock) {
            Map<PartType, Integer> taken = new EnumMap<>(PartType.class);
            int takenCount = 0;

            for (PartType type : PartType.values()) {
                if (takenCount >= maxTake) break;

                int need = needed.getOrDefault(type, 0);
                int available = parts.get(type);

                if (need > 0 && available > 0) {
                    int toTake = Math.min(Math.min(need, available), maxTake - takenCount);
                    parts.put(type, available - toTake);
                    taken.put(type, toTake);
                    takenCount += toTake;
                }
            }

            return taken;
        }
    }

    public Map<PartType, Integer> getParts() {
        return parts;
    }
}
