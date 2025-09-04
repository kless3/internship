package com.internship.innowise.task3;

class FactionAgent implements Runnable {
    private final Faction faction;
    private final Factory factory;
    private volatile boolean running = true;

    public FactionAgent(Faction faction, Factory factory) {
        this.faction = faction;
        this.factory = factory;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        try {
            while (running && factory.getDaysPassed() < 100) {
                int partsCollected = 0;
                while (partsCollected < 5 && factory.getDaysPassed() < 100) {
                    try {
                        PartType part = factory.takePart();
                        faction.addPart(part);
                        partsCollected++;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }

                Thread.sleep(15);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
