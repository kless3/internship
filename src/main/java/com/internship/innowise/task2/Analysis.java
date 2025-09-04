package com.internship.innowise.task2;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
public class Analysis {

    public List<String> getUniqueCities(List<Order> orders) {
        return orders.stream()
                .map(order -> order.getCustomer().getCity())
                .distinct()
                .collect(Collectors.toList());
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

    public List<OrderItem> getMostPopularItems(List<Order> orders) {
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
                .collect(Collectors.toList());
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
}
