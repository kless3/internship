package com.internship.innowise.task1;

import java.util.NoSuchElementException;

public class Main {
    public static void main(String[] args) {

        LinkedList<Integer> list = new LinkedList<>();

        list.addFirst(10);
        list.addLast(30);
        list.add(20, 1);
        list.addLast(40);
        list.addFirst(5);
        System.out.println("List: " + list);
        System.out.println("Size: " + list.size());

        System.out.println("First: " + list.getFirst());
        System.out.println("Last: " + list.getLast());
        System.out.println("Index 2: " + list.get(2));

        try {
            System.out.println("Index 10: " + list.get(10));
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Caught expected exception for get(10): " + e.getMessage());
        }

        Integer removedFirst = list.removeFirst();
        System.out.println("Removed first: " + removedFirst);
        System.out.println("After removeFirst: " + list);

        Integer removedLast = list.removeLast();
        System.out.println("Removed last: " + removedLast);
        System.out.println("After removeLast: " + list);

        Integer removedAtIndex = list.remove(1);
        System.out.println("Removed at index 1: " + removedAtIndex);
        System.out.println("After remove index 1: " + list);
        System.out.println("Size: " + list.size());

        list.add(15, 0);
        list.add(25, 2);
        list.add(20, 2);
        System.out.println("After adding at indexes: " + list);

        System.out.print("Iterating with for-each: ");
        for (Integer value : list) {
            System.out.print(value + " ");
        }
        System.out.println();

        LinkedList<String> stringList = new LinkedList<>();
        System.out.println("Empty list: " + stringList);
        System.out.println("Empty size: " + stringList.size());
        System.out.println("Is empty: " + stringList.isEmpty());

        stringList.addFirst("Hello");
        System.out.println("Single element: " + stringList);

        String removedString = stringList.removeFirst();
        System.out.println("Removed: " + removedString);
        System.out.println("After remove: " + stringList);

        try {
            list.remove(10);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Caught expected exception for remove(10): " + e.getMessage());
        }

        try {
            list.add(100, 10);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Caught expected exception for add(100, 10): " + e.getMessage());
        }

        try {
            stringList.removeFirst();
        } catch (NoSuchElementException e) {
            System.out.println("Caught expected exception for removeFirst on empty list: " + e.getMessage());
        }

        try {
            stringList.getFirst();
        } catch (NoSuchElementException e) {
            System.out.println("Caught expected exception for getFirst on empty list: " + e.getMessage());
        }

        stringList.addLast(null);
        System.out.println("List with null: " + stringList);
        String nullRemoved = stringList.removeFirst();
        System.out.println("Removed null: " + nullRemoved);

        System.out.println("Final list: " + list);
        System.out.println("Final size: " + list.size());
        System.out.println("Is empty: " + list.isEmpty());

        list.addLast(100);
        list.addFirst(0);
        System.out.println("After more operations: " + list);
        System.out.println("First: " + list.getFirst());
        System.out.println("Last: " + list.getLast());
    }
}