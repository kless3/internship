package com.internship.innowise.task3;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Factory factory = new Factory();
        Faction worldFaction = new Faction("World");
        Faction wednesdayFaction = new Faction("Wednesday");

        Thread factoryThread = new Thread(factory);
        Thread worldAgentThread = new Thread(new FactionAgent(worldFaction, factory));
        Thread wednesdayAgentThread = new Thread(new FactionAgent(wednesdayFaction, factory));

        factoryThread.start();
        worldAgentThread.start();
        wednesdayAgentThread.start();

        while (factory.getDaysPassed() < 100) {
            Thread.sleep(100);
        }

        factory.stop();
        worldAgentThread.interrupt();
        wednesdayAgentThread.interrupt();

        factoryThread.join();
        worldAgentThread.join();
        wednesdayAgentThread.join();

        System.out.println("After 100 days: ");
        System.out.println(worldFaction);
        System.out.println(wednesdayFaction);

        int worldRobots = worldFaction.getRobotsBuilt();
        int wednesdayRobots = wednesdayFaction.getRobotsBuilt();

        System.out.println("\nFinal result: ");
        if (worldRobots > wednesdayRobots) {
            System.out.println("World has the strongest army with " + worldRobots + " robots");
        } else if (wednesdayRobots > worldRobots) {
            System.out.println("Wednesday has the strongest army with " + wednesdayRobots + " robots");
        } else {
            System.out.println("Both factions have " + worldRobots + " robots");
        }
    }
}
