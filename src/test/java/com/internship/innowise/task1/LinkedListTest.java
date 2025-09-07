package com.internship.innowise.task1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test class for LinkedList")
class LinkedListTest {

    private LinkedList<Integer> list;

    @BeforeEach
    void setUp() {
        list = new LinkedList<>();
    }

    @Test
    @DisplayName("Check list for empty")
    void testEmptyList() {
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
        assertThrows(NoSuchElementException.class, () -> list.getFirst());
        assertThrows(NoSuchElementException.class, () -> list.getLast());
    }

    @Test
    @DisplayName("Test adding element at the beginning of the list")
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
    @DisplayName("Test adding element at the end of the list")
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
    @DisplayName("Test adding element at specific index")
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
    @DisplayName("Test adding element at invalid index should throw exception")
    void testAddAtIndexOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class, () -> list.add(1, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.add(1, 1));
    }

    @Test
    @DisplayName("Test getting element by index")
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
    @DisplayName("Test removing first element from the list")
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
    @DisplayName("Test removing first element from empty list should throw exception")
    void testRemoveFirstEmptyList() {
        assertThrows(NoSuchElementException.class, () -> list.removeFirst());
    }

    @Test
    @DisplayName("Test removing last element from the list")
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
    @DisplayName("Test removing last element from empty list should throw exception")
    void testRemoveLastEmptyList() {
        assertThrows(NoSuchElementException.class, () -> list.removeLast());
    }

    @Test
    @DisplayName("Test removing element at specific index")
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
    @DisplayName("Test removing element at invalid index should throw exception")
    void testRemoveAtIndexOutOfBounds() {
        list.addLast(1);
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(2));
    }

    @Test
    @DisplayName("Test string representation of the list")
    void testToString() {
        assertEquals("[]", list.toString());

        list.addLast(1);
        assertEquals("[1]", list.toString());

        list.addLast(2);
        list.addLast(3);
        assertEquals("[1, 2, 3]", list.toString());
    }

    @Test
    @DisplayName("Test complex scenario with multiple operations")
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
    @DisplayName("Test LinkedList with String elements")
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
    @DisplayName("Test handling null values in the list")
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
    @DisplayName("Test getting elements from empty list should throw exception")
    void testGetFromEmptyList() {
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(5));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
    }

    @Test
    @DisplayName("Test iterable functionality of the list")
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
    @DisplayName("Test iterator on empty list")
    void testIteratorOnEmptyList() {
        int count = 0;
        for (Integer value : list) {
            count++;
        }
        assertEquals(0, count);
    }

    @Test
    @DisplayName("Test iterator throws exception when no more elements")
    void testIteratorThrowsException() {
        list.addLast(1);
        var iterator = list.iterator();

        assertEquals(1, iterator.next());
        assertThrows(NoSuchElementException.class, iterator::next);
    }
}