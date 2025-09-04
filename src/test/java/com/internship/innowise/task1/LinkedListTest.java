package com.internship.innowise.task1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LinkedListTest {

    private LinkedList<Integer> list;

    @BeforeEach
    void setUp() {
        list = new LinkedList<>();
    }

    @Test
    void testEmptyList() {
        assertEquals(0, list.size());
        assertNull(list.getFirst());
        assertNull(list.getLast());
    }

    @Test
    void testAddFirst() {
        list.addFirst(1);
        assertEquals(1, list.size());
        assertEquals(1, list.getFirst());
        assertEquals(1, list.getLast());

        list.addFirst(2);
        assertEquals(2, list.size());
        assertEquals(2, list.getFirst());
        assertEquals(1, list.getLast());
    }

    @Test
    void testAddLast() {
        list.addLast(1);
        assertEquals(1, list.size());
        assertEquals(1, list.getFirst());
        assertEquals(1, list.getLast());

        list.addLast(2);
        assertEquals(2, list.size());
        assertEquals(1, list.getFirst());
        assertEquals(2, list.getLast());
    }

    @Test
    void testAddAtIndex() {
        list.add(1, 0);
        assertEquals(1, list.getFirst());

        list.add(0, 0);
        assertEquals(0, list.getFirst());
        assertEquals(1, list.getLast());

        list.add(2, 2);
        list.add(99, 1);
        assertEquals(4, list.size());
        assertEquals(99, list.get(1));
    }

    @Test
    void testAddAtIndexOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class, () -> list.add(1, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.add(1, 1));
    }

    @Test
    void testGet() {
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);

        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(3, list.get(2));
        assertNull(list.get(3));
        assertNull(list.get(-1));
    }

    @Test
    void testRemoveFirst() {
        list.addLast(1);
        list.addLast(2);

        list.removeFirst();
        assertEquals(1, list.size());
        assertEquals(2, list.getFirst());

        list.removeFirst();
        assertNull(list.getFirst());
    }

    @Test
    void testRemoveFirstEmptyList() {
        assertDoesNotThrow(() -> list.removeFirst());
    }

    @Test
    void testRemoveLast() {
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);

        list.removeLast();
        assertEquals(2, list.size());
        assertEquals(2, list.getLast());

        list.removeLast();
        assertEquals(1, list.size());
        assertEquals(1, list.getLast());

    }

    @Test
    void testRemoveLastSingleElement() {
        list.addLast(1);
        list.removeLast();
        assertNull(list.getFirst());
    }

    @Test
    void testRemoveAtIndex() {
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);
        list.addLast(4);

        list.remove(1);
        assertEquals(3, list.size());
        assertEquals(3, list.get(1));

        list.remove(0);
        assertEquals(2, list.size());
        assertEquals(3, list.getFirst());

        list.remove(1);
        assertEquals(1, list.size());
        assertEquals(3, list.getLast());
    }

    @Test
    void testRemoveAtIndexOutOfBounds() {
        list.addLast(1);
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(1));
    }

    @Test
    void testToString() {
        assertEquals("[]", list.toString());

        list.addLast(1);
        assertEquals("[1]", list.toString());

        list.addLast(2);
        list.addLast(3);
        assertEquals("[1, 2, 3]", list.toString());
    }

    @Test
    void testComplexScenario() {

        list.addFirst(10);
        assertEquals(1, list.size());
        assertEquals(10, list.getFirst());

        list.addLast(20);
        assertEquals(2, list.size());
        assertEquals(20, list.getLast());

        list.add(15, 1);
        assertEquals(3, list.size());
        assertEquals(15, list.get(1));

        list.remove(1);
        assertEquals(2, list.size());
        assertEquals(20, list.get(1));

        list.removeFirst();
        assertEquals(1, list.size());
        assertEquals(20, list.getFirst());

    }

    @Test
    void testStringList() {
        LinkedList<String> stringList = new LinkedList<>();
        stringList.addLast("Hello");
        stringList.addLast("World");

        assertEquals(2, stringList.size());
        assertEquals("Hello", stringList.getFirst());
        assertEquals("World", stringList.getLast());
    }

    @Test
    void testNullValues() {
        list.addLast(null);
        assertEquals(1, list.size());
        assertNull(list.getFirst());
        assertNull(list.get(0));
    }

    @Test
    void testGetFromEmptyList() {
        assertNull(list.get(0));
        assertNull(list.get(5));
        assertNull(list.get(-1));
    }
}
