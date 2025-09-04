package com.internship.innowise.task1;

public class LinkedList<T> {

    private Node<T> head;
    private int size;

    public LinkedList() {
        this.head = null;
        this.size = 0;
    }

    public int size() {
        return size;
    }

    public void addFirst(T t) {
        Node<T> firstNode = new Node<>(t);
        firstNode.next = head;
        head = firstNode;
        size++;
    }

    public void addLast(T t) {
        if (head == null) {
            addFirst(t);
            return;
        }

        Node<T> currentNode = head;
        while (currentNode.next != null) {
            currentNode = currentNode.next;
        }
        currentNode.next = new Node<>(t);
        size++;
    }

    public void add(T t, int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        if (index == 0) {
            addFirst(t);
            return;
        }

        if (index == size) {
            addLast(t);
            return;
        }

        Node<T> currentNode = head;
        for (int i = 0; i < index - 1; i++) {
            currentNode = currentNode.next;
        }

        Node<T> newNode = new Node<>(t);
        newNode.next = currentNode.next;
        currentNode.next = newNode;
        size++;
    }

    public T getFirst() {
        if (head == null) {
            return null;
        }
        return head.value;
    }

    public T getLast() {
        if (head == null) {
            return null;
        }

        Node<T> currentNode = head;
        while (currentNode.next != null) {
            currentNode = currentNode.next;
        }
        return currentNode.value;
    }

    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }

        Node<T> currentNode = head;
        for (int i = 0; i < index; i++) {
            currentNode = currentNode.next;
        }
        return currentNode.value;
    }

    public void removeFirst() {
        if (head == null) {
            return;
        }
        head = head.next;
        size--;
    }

    public void removeLast() {
        if (head == null) {
            return;
        }

        if (size == 1) {
            head = null;
            size = 0;
            return;
        }

        Node<T> currentNode = head;
        for (int i = 0; i < size - 2; i++) {
            currentNode = currentNode.next;
        }
        currentNode.next = null;
        size--;
    }

    public void remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        if (index == 0) {
            removeFirst();
            return;
        }

        Node<T> currentNode = head;
        for (int i = 0; i < index - 1; i++) {
            currentNode = currentNode.next;
        }
        currentNode.next = currentNode.next.next;
        size--;
    }

    @Override
    public String toString() {
        if (head == null) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        Node<T> currentNode = head;
        while (currentNode != null) {
            sb.append(currentNode.value);
            if (currentNode.next != null) {
                sb.append(", ");
            }
            currentNode = currentNode.next;
        }
        sb.append("]");
        return sb.toString();
    }

    private static class Node<T> {
        private final T value;
        private Node<T> next;

        private Node(T value) {
            this.value = value;
            this.next = null;
        }
    }
}