package com.internship.innowise.task3;

import java.util.concurrent.*;

public class RobotSimulation {
    public static void main(String[] args) throws InterruptedException {
        Factory factory = new Factory();
        CyclicBarrier barrier = new CyclicBarrier(3);

        Faction world = new Faction("World", factory, barrier);
        Faction wednesday = new Faction("Wednesday", factory, barrier);

        Thread factoryThread = new Thread(() -> {
            try {
                for (int day = 0; day < 100; day++) {
                    System.out.println("Day " + (day + 1));
                    factory.produceParts();
                    barrier.await();
                    barrier.await();
                    System.out.println("Night ended\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Thread worldThread = new Thread(world);
        Thread wednesdayThread = new Thread(wednesday);

        factoryThread.start();
        worldThread.start();
        wednesdayThread.start();

        factoryThread.join();
        worldThread.join();
        wednesdayThread.join();

        System.out.println(world.getName() + " built robots: " + world.getRobotsBuilt());
        System.out.println(wednesday.getName() + " built robots: " + wednesday.getRobotsBuilt());

        if (world.getRobotsBuilt() > wednesday.getRobotsBuilt()) {
            System.out.println("Winner: " + world.getName());
        } else if (world.getRobotsBuilt() < wednesday.getRobotsBuilt()) {
            System.out.println("Winner: " + wednesday.getName());
        } else {
            System.out.println("Friendship=)");
        }
    }
}
