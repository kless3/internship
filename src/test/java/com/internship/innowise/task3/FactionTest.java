package com.internship.innowise.task3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FactionTest {
    private Faction faction;

    @BeforeEach
    void setUp() {
        faction = new Faction("TestFaction");
    }

    @Test
    void testFactionCreation() {
        assertEquals("TestFaction", faction.toString().split(" - ")[0]);
        assertEquals(0, faction.getRobotsBuilt());
        Map<PartType, Integer> inventory = faction.getPartsInventory();
        for (PartType type : PartType.values()) {
            assertEquals(0, inventory.get(type));
        }
    }

    @Test
    void testAddPart() {
        faction.addPart(PartType.HEAD);
        Map<PartType, Integer> inventory = faction.getPartsInventory();
        assertEquals(1, inventory.get(PartType.HEAD));
        for (PartType type : PartType.values()) {
            if (type != PartType.HEAD) {
                assertEquals(0, inventory.get(type));
            }
        }
    }

    @Test
    void testTryBuildRobotWithInsufficientParts() {
        faction.addPart(PartType.HEAD);
        faction.addPart(PartType.TORSO);
        assertEquals(0, faction.getRobotsBuilt());
        Map<PartType, Integer> inventory = faction.getPartsInventory();
        assertTrue(inventory.values().stream().anyMatch(count -> count == 0));
    }

    @Test
    void testTryBuildRobotWithSufficientParts() {
        for (PartType type : PartType.values()) {
            faction.addPart(type);
        }
        assertEquals(1, faction.getRobotsBuilt());
        Map<PartType, Integer> inventory = faction.getPartsInventory();
        for (int count : inventory.values()) {
            assertEquals(0, count);
        }
    }

    @Test
    void testMultipleRobotBuilds() {
        for (int i = 0; i < 2; i++) {
            for (PartType type : PartType.values()) {
                faction.addPart(type);
            }
        }
        assertEquals(2, faction.getRobotsBuilt());
        Map<PartType, Integer> inventory = faction.getPartsInventory();
        for (int count : inventory.values()) {
            assertEquals(0, count);
        }
    }

    @Test
    void testGetPartsInventoryReturnsCopy() {
        Map<PartType, Integer> inventory1 = faction.getPartsInventory();
        faction.addPart(PartType.HEAD);
        Map<PartType, Integer> inventory2 = faction.getPartsInventory();
        assertNotSame(inventory1, inventory2);
        assertNotEquals(inventory1.get(PartType.HEAD), inventory2.get(PartType.HEAD));
    }
}