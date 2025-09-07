package com.internship.innowise.task3;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;

class Faction implements Runnable {
    private final String name;
    private final Factory factory;
    private final Map<PartType, Integer> inventory = new EnumMap<>(PartType.class);
    private int robotsBuilt = 0;
    private final CyclicBarrier barrier;

    public Faction(String name, Factory factory, CyclicBarrier barrier) {
        this.name = name;
        this.factory = factory;
        this.barrier = barrier;
        for (PartType part : PartType.values()) inventory.put(part, 0);
    }

    @Override
    public void run() {
        try {
            for (int day = 0; day < 100; day++) {
                barrier.await();

                System.out.println("Night for " + name + " begins. Current inventory: " + inventory);

                Map<PartType, Integer> needed = new EnumMap<>(PartType.class);
                for (PartType part : PartType.values()) {
                    needed.put(part, Math.max(0, 1 - inventory.get(part)));
                }

                Map<PartType, Integer> taken = factory.takeParts(needed, 5);
                for (PartType part : PartType.values()) {
                    int count = taken.getOrDefault(part, 0);
                    if (count > 0) {
                        inventory.put(part, inventory.get(part) + count);
                        System.out.println(name + " took " + count + " " + part);
                    }
                }

                while (canBuildRobot()) {
                    robotsBuilt++;
                    for (PartType part : PartType.values()) {
                        inventory.put(part, inventory.get(part) - 1);
                    }
                    System.out.println(name + " built a robot! Total robots: " + robotsBuilt);
                }

                System.out.println("Night for " + name + " ends. Inventory after night: " + inventory + "\n");

                barrier.await();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean canBuildRobot() {
        for (PartType part : PartType.values()) {
            if (inventory.get(part) <= 0) return false;
        }
        return true;
    }

    public int getRobotsBuilt() {
        return robotsBuilt;
    }

    public String getName() {
        return name;
    }

    public Map<PartType, Integer> getInventory() {
        return inventory;
    }

}
