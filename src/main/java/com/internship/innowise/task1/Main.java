package com.internship.innowise.task1;

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
        System.out.println("Index 10: " + list.get(10));

        list.removeFirst();
        System.out.println("After removeFirst: " + list);

        list.removeLast();
        System.out.println("After removeLast: " + list);

        list.remove(1);
        System.out.println("After remove index 1: " + list);
        System.out.println("Size: " + list.size());

        list.add(15, 0);
        list.add(25, 2);
        list.add(20, 2);
        System.out.println("After adding at indexes: " + list);

        LinkedList<String> stringList = new LinkedList<>();
        System.out.println("Empty list: " + stringList);
        System.out.println("Empty size: " + stringList.size());

        stringList.addFirst("Hello");
        System.out.println("Single element: " + stringList);
        stringList.removeFirst();
        System.out.println("After remove: " + stringList);

        try {
            list.remove(10);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Caught expected exception: " + e.getMessage());
        }

        try {
            list.add(100, 10);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Caught expected exception: " + e.getMessage());
        }

        System.out.println("Final list: " + list);
        System.out.println("Final size: " + list.size());
    }
}
