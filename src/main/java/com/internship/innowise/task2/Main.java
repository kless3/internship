package com.internship.innowise.task2;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        Analysis analysis = new Analysis();

        Customer customer1 = new Customer();
        customer1.setCustomerId("1");
        customer1.setName("John");
        customer1.setCity("New York");

        Customer customer2 = new Customer();
        customer2.setCustomerId("2");
        customer2.setName("Alice");
        customer2.setCity("Los Angeles");

        Customer customer3 = new Customer();
        customer3.setCustomerId("3");
        customer3.setName("Bob");
        customer3.setCity("New York");

        OrderItem item1 = new OrderItem();
        item1.setProductName("Laptop");
        item1.setQuantity(1);
        item1.setPrice(1000.0);

        OrderItem item2 = new OrderItem();
        item2.setProductName("Mouse");
        item2.setQuantity(2);
        item2.setPrice(25.0);

        OrderItem item3 = new OrderItem();
        item3.setProductName("Keyboard");
        item3.setQuantity(1);
        item3.setPrice(75.0);

        OrderItem item4 = new OrderItem();
        item4.setProductName("Laptop");
        item4.setQuantity(1);
        item4.setPrice(1000.0);

        OrderItem item5 = new OrderItem();
        item5.setProductName("Mouse");
        item5.setQuantity(3);
        item5.setPrice(25.0);

        Order order1 = new Order();
        order1.setOrderId("001");
        order1.setCustomer(customer1);
        order1.setItems(List.of(item1, item2));
        order1.setStatus(OrderStatus.DELIVERED);

        Order order2 = new Order();
        order2.setOrderId("002");
        order2.setCustomer(customer2);
        order2.setItems(List.of(item3, item4));
        order2.setStatus(OrderStatus.DELIVERED);

        Order order3 = new Order();
        order3.setOrderId("003");
        order3.setCustomer(customer3);
        order3.setItems(List.of(item5));
        order3.setStatus(OrderStatus.PROCESSING);

        Order order4 = new Order();
        order4.setOrderId("004");
        order4.setCustomer(customer1);
        order4.setItems(List.of(item2, item3));
        order4.setStatus(OrderStatus.DELIVERED);

        Order order5 = new Order();
        order5.setOrderId("005");
        order5.setCustomer(customer1);
        order5.setItems(List.of(item1, item5));
        order5.setStatus(OrderStatus.CANCELLED);

        List<Order> orders = List.of(order1, order2, order3, order4, order5);

        System.out.println("Unique cities: " + analysis.getUniqueCities(orders));
        System.out.println("Total income: " + analysis.getIncome(orders));
        System.out.println("Most popular items: " + analysis.getMostPopularItems(orders));
        System.out.println("Average check: " + analysis.getAvgCheck(orders));
        System.out.println("Loyal customers: " + analysis.getLoyalCustomers(orders));
    }
}
