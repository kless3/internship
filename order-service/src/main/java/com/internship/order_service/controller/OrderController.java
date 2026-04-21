package com.internship.order_service.controller;

import com.internship.order_service.dto.OrderEventResponseDto;
import com.internship.order_service.dto.OrderRequestDTO;
import com.internship.order_service.dto.OrderResponseDTO;
import com.internship.order_service.dto.ItemDTO;
import com.internship.order_service.model.enums.OrderStatus;
import com.internship.order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long id){
        return new ResponseEntity<>(orderService.getOrderById(id), HttpStatus.OK);
    }

    @GetMapping("/ids")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByIds(@RequestParam List<Long> ids){
        return new ResponseEntity<>(orderService.getOrdersByIds(ids), HttpStatus.OK);
    }

    @GetMapping("/status")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByStatus(@RequestParam OrderStatus status){
        return new ResponseEntity<>(orderService.getOrdersByStatus(status), HttpStatus.OK);
    }

    @GetMapping("/items")
    public ResponseEntity<Page<ItemDTO>> getAvailableItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return new ResponseEntity<>(orderService.getAllAvailableItems(page, size), HttpStatus.OK);
    }

    @GetMapping("/current")
    public ResponseEntity<Page<OrderResponseDTO>> getOrdersByUserEmail(
            @RequestHeader("X-User-Id") String userEmail,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return new ResponseEntity<>(orderService.getOrdersByUserEmail(userEmail, page, size), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderRequestDTO orderRequestDTO){
        return new ResponseEntity<>(orderService.createOrder(orderRequestDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> updateOrder(@PathVariable Long id, @Valid @RequestBody OrderRequestDTO orderRequestDTO){
        return new ResponseEntity<>(orderService.updateOrderById(id, orderRequestDTO), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id){
        orderService.deleteOrderById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<OrderEventResponseDto>> getOrderHistory(@PathVariable Long id) {
        return new ResponseEntity<>(orderService.getOrderHistory(id), HttpStatus.OK);
    }
}
