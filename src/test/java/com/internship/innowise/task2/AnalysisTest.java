package com.internship.innowise.task2;


import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnalysisTest {

    @Test
    void testGetUniqueCities() {
        Analysis analysis = new Analysis();

        Customer customer1 = createCustomer("Moscow");
        Customer customer2 = createCustomer("SPb");
        Customer customer3 = createCustomer("Moscow");

        Order order1 = createOrder(customer1, OrderStatus.DELIVERED);
        Order order2 = createOrder(customer2, OrderStatus.DELIVERED);
        Order order3 = createOrder(customer3, OrderStatus.DELIVERED);

        List<Order> orders = List.of(order1, order2, order3);

        Set<String> result = analysis.getUniqueCities(orders);

        assertEquals(2, result.size());
        assertTrue(result.contains("Moscow"));
        assertTrue(result.contains("SPb"));
    }

    @Test
    void testGetUniqueCitiesEmptyList() {
        Analysis analysis = new Analysis();
        List<Order> orders = List.of();

        Set<String> result = analysis.getUniqueCities(orders);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetIncome() {
        Analysis analysis = new Analysis();

        OrderItem item1 = createOrderItem("Laptop", 1, 1000.0);
        OrderItem item2 = createOrderItem("Mouse", 2, 50.0);
        Order deliveredOrder = createOrderWithItems(OrderStatus.DELIVERED, item1, item2);

        OrderItem item3 = createOrderItem("Keyboard", 1, 100.0);
        Order cancelledOrder = createOrderWithItems(OrderStatus.CANCELLED, item3);

        List<Order> orders = List.of(deliveredOrder, cancelledOrder);

        double result = analysis.getIncome(orders);

        assertEquals(1100.0, result, 0.001);
    }

    @Test
    void testGetIncomeNoDeliveredOrders() {
        Analysis analysis = new Analysis();

        OrderItem item1 = createOrderItem("Monitor", 1, 300.0);
        Order cancelledOrder = createOrderWithItems(OrderStatus.CANCELLED, item1);

        List<Order> orders = List.of(cancelledOrder);

        double result = analysis.getIncome(orders);

        assertEquals(0.0, result, 0.001);
    }

    @Test
    void testGetMostPopularItem() {
        Analysis analysis = new Analysis();

        OrderItem popularItem = createOrderItem("Book", 5, 20.0);
        OrderItem regularItem = createOrderItem("Pen", 2, 5.0);
        OrderItem anotherPopular = createOrderItem("Book", 3, 20.0);

        Order order1 = createOrderWithItems(OrderStatus.DELIVERED, popularItem, regularItem);
        Order order2 = createOrderWithItems(OrderStatus.DELIVERED, anotherPopular);

        List<Order> orders = List.of(order1, order2);

        Optional<OrderItem> result = analysis.getMostPopularItem(orders);

        assertEquals("Book", result.get().getProductName());
        assertEquals(8, result.get().getQuantity());
    }

    @Test
    void testGetAvgCheck() {
        Analysis analysis = new Analysis();

        OrderItem item1 = createOrderItem("Tablet", 1, 500.0);
        OrderItem item2 = createOrderItem("Case", 1, 50.0);
        Order order1 = createOrderWithItems(OrderStatus.DELIVERED, item1);
        Order order2 = createOrderWithItems(OrderStatus.DELIVERED, item2);

        OrderItem item3 = createOrderItem("Phone", 1, 800.0);
        Order cancelledOrder = createOrderWithItems(OrderStatus.CANCELLED, item3);

        List<Order> orders = List.of(order1, order2, cancelledOrder);

        double result = analysis.getAvgCheck(orders);

        assertEquals(275.0, result, 0.001);
    }

    @Test
    void testGetAvgCheckNoDeliveredOrders() {
        Analysis analysis = new Analysis();

        OrderItem item = createOrderItem("Headphones", 1, 150.0);
        Order cancelledOrder = createOrderWithItems(OrderStatus.CANCELLED, item);

        List<Order> orders = List.of(cancelledOrder);

        double result = analysis.getAvgCheck(orders);

        assertEquals(0.0, result, 0.001);
    }

    @Test
    void testGetLoyalCustomers() {
        Analysis analysis = new Analysis();

        Customer loyalCustomer = createCustomer("Moscow");
        Customer regularCustomer = createCustomer("SPb");

        Order order1 = createOrder(loyalCustomer, OrderStatus.DELIVERED);
        Order order2 = createOrder(loyalCustomer, OrderStatus.DELIVERED);
        Order order3 = createOrder(loyalCustomer, OrderStatus.DELIVERED);
        Order order4 = createOrder(loyalCustomer, OrderStatus.DELIVERED);
        Order order5 = createOrder(loyalCustomer, OrderStatus.DELIVERED);
        Order order6 = createOrder(loyalCustomer, OrderStatus.DELIVERED);
        Order order7 = createOrder(regularCustomer, OrderStatus.DELIVERED);

        List<Order> orders = List.of(order1, order2, order3, order4, order5, order6, order7);

        List<Customer> result = analysis.getLoyalCustomers(orders);

        assertEquals(1, result.size());
        assertEquals(loyalCustomer, result.get(0));
    }

    @Test
    void testGetLoyalCustomersNoLoyal() {
        Analysis analysis = new Analysis();

        Customer customer = createCustomer("Moscow");
        Order order = createOrder(customer, OrderStatus.DELIVERED);

        List<Order> orders = List.of(order);

        List<Customer> result = analysis.getLoyalCustomers(orders);

        assertTrue(result.isEmpty());
    }

    private Customer createCustomer(String city) {
        Customer customer = new Customer();
        customer.setCity(city);
        return customer;
    }

    private Order createOrder(Customer customer, OrderStatus status) {
        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(status);
        order.setItems(List.of(createOrderItem("Test", 1, 10.0)));
        return order;
    }

    private Order createOrderWithItems(OrderStatus status, OrderItem... items) {
        Order order = new Order();
        order.setStatus(status);
        order.setItems(List.of(items));
        order.setCustomer(createCustomer("TestCity"));
        return order;
    }

    private OrderItem createOrderItem(String name, int quantity, double price) {
        OrderItem item = new OrderItem();
        item.setProductName(name);
        item.setQuantity(quantity);
        item.setPrice(price);
        return item;
    }
}