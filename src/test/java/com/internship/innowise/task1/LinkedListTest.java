package com.internship.innowise.task1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

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
        assertTrue(list.isEmpty());
        assertThrows(NoSuchElementException.class, () -> list.getFirst());
        assertThrows(NoSuchElementException.class, () -> list.getLast());
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
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(3));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
    }

    @Test
    void testRemoveFirst() {
        list.addLast(1);
        list.addLast(2);

        Integer removed = list.removeFirst();
        assertEquals(1, removed);
        assertEquals(1, list.size());
        assertEquals(2, list.getFirst());

        removed = list.removeFirst();
        assertEquals(2, removed);
        assertEquals(0, list.size());
        assertThrows(NoSuchElementException.class, () -> list.getFirst());
    }

    @Test
    void testRemoveFirstEmptyList() {
        assertThrows(NoSuchElementException.class, () -> list.removeFirst());
    }

    @Test
    void testRemoveLast() {
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);

        Integer removed = list.removeLast();
        assertEquals(3, removed);
        assertEquals(2, list.size());
        assertEquals(2, list.getLast());

        removed = list.removeLast();
        assertEquals(2, removed);
        assertEquals(1, list.size());
        assertEquals(1, list.getLast());

        removed = list.removeLast();
        assertEquals(1, removed);
        assertEquals(0, list.size());
        assertThrows(NoSuchElementException.class, () -> list.getLast());
    }

    @Test
    void testRemoveLastEmptyList() {
        assertThrows(NoSuchElementException.class, () -> list.removeLast());
    }

    @Test
    void testRemoveAtIndex() {
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);
        list.addLast(4);

        Integer removed = list.remove(1);
        assertEquals(2, removed);
        assertEquals(3, list.size());
        assertEquals(3, list.get(1));

        removed = list.remove(0);
        assertEquals(1, removed);
        assertEquals(2, list.size());
        assertEquals(3, list.getFirst());

        removed = list.remove(1);
        assertEquals(4, removed);
        assertEquals(1, list.size());
        assertEquals(3, list.getLast());
    }

    @Test
    void testRemoveAtIndexOutOfBounds() {
        list.addLast(1);
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(2));
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

        Integer removed = list.remove(1);
        assertEquals(15, removed);
        assertEquals(2, list.size());
        assertEquals(20, list.get(1));

        removed = list.removeFirst();
        assertEquals(10, removed);
        assertEquals(1, list.size());
        assertEquals(20, list.getFirst());

        removed = list.removeLast();
        assertEquals(20, removed);
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
    }

    @Test
    void testStringList() {
        LinkedList<String> stringList = new LinkedList<>();
        stringList.addLast("Hello");
        stringList.addLast("World");

        assertEquals(2, stringList.size());
        assertEquals("Hello", stringList.getFirst());
        assertEquals("World", stringList.getLast());

        String removed = stringList.removeFirst();
        assertEquals("Hello", removed);
        assertEquals("World", stringList.getFirst());
    }

    @Test
    void testNullValues() {
        list.addLast(null);
        assertEquals(1, list.size());
        assertNull(list.getFirst());
        assertNull(list.get(0));

        Integer removed = list.removeFirst();
        assertNull(removed);
        assertEquals(0, list.size());
    }

    @Test
    void testGetFromEmptyList() {
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(5));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
    }

    @Test
    void testIterable() {
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);

        int sum = 0;
        int count = 0;
        for (Integer value : list) {
            sum += value;
            count++;
        }

        assertEquals(6, sum);
        assertEquals(3, count);
    }

    @Test
    void testIteratorOnEmptyList() {
        int count = 0;
        for (Integer value : list) {
            count++;
        }
        assertEquals(0, count);
    }

    @Test
    void testIteratorThrowsException() {
        list.addLast(1);
        var iterator = list.iterator();

        assertEquals(1, iterator.next());
        assertThrows(NoSuchElementException.class, iterator::next);
    }
}