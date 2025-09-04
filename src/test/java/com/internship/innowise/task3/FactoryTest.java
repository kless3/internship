package com.internship.innowise.task3;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class FactoryTest {
    private Factory factory;

    @AfterEach
    void tearDown() {
        if (factory != null) {
            factory.stop();
        }
    }

    @Test
    void testFactoryInitialState() {
        factory = new Factory();
        assertEquals(0, factory.getDaysPassed());
    }

    @Test
    void testFactoryRunProducesParts() throws InterruptedException {
        factory = new Factory();
        Thread factoryThread = new Thread(factory);
        factoryThread.start();
        Thread.sleep(50);
        PartType part = factory.takePart();
        assertNotNull(part);
        factory.stop();
        factoryThread.join(1000);
    }

    @Test
    void testFactoryDaysIncrement() throws InterruptedException {
        factory = new Factory();
        Thread factoryThread = new Thread(factory);
        factoryThread.start();
        Thread.sleep(30);
        int daysPassed = factory.getDaysPassed();
        assertTrue(daysPassed > 0);
        factory.stop();
        factoryThread.join(1000);
    }

    @Test
    void testTakePartBlocksWhenEmpty() throws InterruptedException {
        factory = new Factory();
        AtomicBoolean partTaken = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        Thread consumer = new Thread(() -> {
            try {
                latch.countDown();
                factory.takePart();
                partTaken.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        consumer.start();
        latch.await();
        Thread.sleep(50);
        assertFalse(partTaken.get());
        Thread factoryThread = new Thread(factory);
        factoryThread.start();
        Thread.sleep(30);
        factory.stop();
        consumer.interrupt();
        factoryThread.interrupt();
        consumer.join(1000);
        factoryThread.join(1000);
    }
}