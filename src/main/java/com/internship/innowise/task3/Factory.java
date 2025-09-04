package com.internship.innowise.task3;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

class Factory implements Runnable {
    private final BlockingQueue<PartType> productionLine = new LinkedBlockingQueue<>();
    private final Random random = new Random();
    private volatile boolean running = true;
    private final AtomicInteger daysPassed = new AtomicInteger(0);

    public void stop() {
        running = false;
    }

    public PartType takePart() throws InterruptedException {
        return productionLine.take();
    }

    public int getDaysPassed() {
        return daysPassed.get();
    }

    @Override
    public void run() {
        try {
            while (running && daysPassed.get() < 100) {
                int partsToProduce = random.nextInt(11);
                for (int i = 0; i < partsToProduce; i++) {
                    PartType[] allTypes = PartType.values();
                    PartType randomPart = allTypes[random.nextInt(allTypes.length)];
                    productionLine.put(randomPart);
                }

                daysPassed.incrementAndGet();
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
