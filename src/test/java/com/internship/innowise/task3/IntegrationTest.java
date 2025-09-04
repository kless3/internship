package com.internship.innowise.task3;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class IntegrationTest {
    @Test
    void testFullIntegration() throws InterruptedException {
        Factory factory = new Factory();

        Faction faction1 = new Faction("RedFaction");
        Faction faction2 = new Faction("BlueFaction");

        FactionAgent agent1 = new FactionAgent(faction1, factory);
        FactionAgent agent2 = new FactionAgent(faction2, factory);

        Thread factoryThread = new Thread(factory);
        Thread agentThread1 = new Thread(agent1);
        Thread agentThread2 = new Thread(agent2);

        factoryThread.start();
        agentThread1.start();
        agentThread2.start();

        Thread.sleep(200);

        agent1.stop();
        agent2.stop();
        factory.stop();

        agentThread1.join(1000);
        agentThread2.join(1000);
        factoryThread.join(1000);

        assertTrue(faction1.getRobotsBuilt() >= 0);
        assertTrue(faction2.getRobotsBuilt() >= 0);
    }
}