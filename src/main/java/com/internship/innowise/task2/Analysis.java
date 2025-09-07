package com.internship.innowise.task2;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
public class Analysis {

    public Set<String> getUniqueCities(List<Order> orders) {
        return orders.stream()
                .map(order -> order.getCustomer().getCity())
                .distinct()
                .collect(Collectors.toSet());
    }

    public double getIncome(List<Order> orders) {
        return orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .mapToDouble(order -> order.getItems()
                        .stream()
                        .mapToDouble(item -> item.getPrice() * item.getQuantity())
                        .sum())
                .sum();
    }

    public Optional<OrderItem> getMostPopularItem(List<Order> orders) {
        return orders.stream()
                .flatMap(order -> order.getItems().stream())
                .collect(Collectors.toMap(
                        OrderItem::getProductName,
                        item -> item,
                        (item1, item2) -> {
                            OrderItem merged = new OrderItem();
                            merged.setProductName(item1.getProductName());
                            merged.setQuantity(item1.getQuantity() + item2.getQuantity());
                            merged.setPrice(item1.getPrice());
                            return merged;
                        }
                ))
                .values()
                .stream()
                .sorted((i1, i2) -> i2.getQuantity() - i1.getQuantity())
                .findFirst();
    }

    public double getAvgCheck(List<Order> orders) {
        return orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .mapToDouble(order -> order.getItems()
                        .stream()
                        .mapToDouble(item -> item.getQuantity() * item.getPrice())
                        .sum())
                .average()
                .orElse(0.0);
    }

    public List<Customer> getLoyalCustomers(List<Order> orders) {
        return orders.stream()
                .collect(Collectors.groupingBy(
                        Order::getCustomer,
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<Order> getTestOrders(){

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
        return orders;
    }

}
