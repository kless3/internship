package com.internship.order_service.controller;

import com.internship.order_service.dto.OrderEventResponseDto;
import com.internship.order_service.dto.OrderRequestDTO;
import com.internship.order_service.dto.OrderResponseDTO;
import com.internship.order_service.model.enums.OrderStatus;
import com.internship.order_service.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
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
    public ResponseEntity<OrderResponseDTO> updateOrder(
            @PathVariable Long id, @Valid @RequestBody OrderRequestDTO orderRequestDTO){
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

    @PostMapping("/{id}/restore")
    public ResponseEntity<OrderResponseDTO> restoreOrderStatusAt(@PathVariable Long id,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date
    ) {
        return new ResponseEntity<>(orderService.restoreOrderStatusAt(id, date), HttpStatus.OK);
    }
}
