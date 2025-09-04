package com.internship.innowise.task3;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

class Faction {
    private final String name;
    private final Map<PartType, AtomicInteger> parts = new HashMap<>();
    private final AtomicInteger robotsBuilt = new AtomicInteger(0);

    public Faction(String name) {
        this.name = name;
        for (PartType type : PartType.values()) {
            parts.put(type, new AtomicInteger(0));
        }
    }

    public void addPart(PartType type) {
        parts.get(type).incrementAndGet();
        tryBuildRobot();
    }

    private void tryBuildRobot() {
        boolean canBuild = true;
        for (AtomicInteger count : parts.values()) {
            if (count.get() < 1) {
                canBuild = false;
                break;
            }
        }

        if (canBuild) {
            for (AtomicInteger count : parts.values()) {
                count.decrementAndGet();
            }
            robotsBuilt.incrementAndGet();
        }
    }

    public int getRobotsBuilt() {
        return robotsBuilt.get();
    }

    public Map<PartType, Integer> getPartsInventory() {
        Map<PartType, Integer> inventory = new HashMap<>();
        for (Map.Entry<PartType, AtomicInteger> entry : parts.entrySet()) {
            inventory.put(entry.getKey(), entry.getValue().get());
        }
        return inventory;
    }

    @Override
    public String toString() {
        return name + " - Robots: " + robotsBuilt.get() +
                ", Parts: " + getPartsInventory();
    }
}
