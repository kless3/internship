package com.internship.innowise.task3;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FactionAgentTest {
    private Faction faction;
    private Factory factory;
    private FactionAgent agent;

    @BeforeEach
    void setUp() {
        faction = new Faction("TestFaction");
        factory = new Factory();
        agent = new FactionAgent(faction, factory);
    }

    @AfterEach
    void tearDown() {
        agent.stop();
        factory.stop();
    }

    @Test
    void testFactionAgentCreation() {
        assertNotNull(agent);
    }

    @Test
    void testFactionAgentRun() throws InterruptedException {
        Thread agentThread = new Thread(agent);
        Thread factoryThread = new Thread(factory);
        factoryThread.start();
        agentThread.start();
        Thread.sleep(100);
        Map<PartType, Integer> inventory = faction.getPartsInventory();
        boolean hasParts = inventory.values().stream().anyMatch(count -> count > 0);
        assertTrue(hasParts || faction.getRobotsBuilt() > 0);
        agent.stop();
        factory.stop();
        agentThread.join(1000);
        factoryThread.join(1000);
    }

    @Test
    void testFactionAgentInterruptedException() throws InterruptedException {
        FactionAgent agent = new FactionAgent(faction, factory);
        Thread agentThread = new Thread(agent);
        agentThread.start();
        Thread.sleep(10);
        agentThread.interrupt();
        agentThread.join(1000);
        assertFalse(agentThread.isAlive());
    }
}