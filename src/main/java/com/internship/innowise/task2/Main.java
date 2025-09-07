package com.internship.innowise.task2;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        Analysis analysis = new Analysis();

        List<Order> orders = analysis.getTestOrders();

        System.out.println("Unique cities: " + analysis.getUniqueCities(orders));
        System.out.println("Total income: " + analysis.getIncome(orders));
        System.out.println("Most popular items: " + analysis.getMostPopularItem(orders));
        System.out.println("Average check: " + analysis.getAvgCheck(orders));
        System.out.println("Loyal customers: " + analysis.getLoyalCustomers(orders));
    }
}
