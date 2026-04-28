package com.internship.order_service.controller;

import com.internship.order_service.dto.response.OrderEventResponseDto;
import com.internship.order_service.dto.request.OrderRequestDto;
import com.internship.order_service.dto.response.OrderResponseDto;
import com.internship.order_service.dto.request.UpdateShippingAddressRequestDto;
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
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long id){
        return new ResponseEntity<>(orderService.getOrderById(id), HttpStatus.OK);
    }

    @GetMapping("/ids")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByIds(@RequestParam List<Long> ids){
        return new ResponseEntity<>(orderService.getOrdersByIds(ids), HttpStatus.OK);
    }

    @GetMapping("/status")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByStatus(@RequestParam OrderStatus status){
        return new ResponseEntity<>(orderService.getOrdersByStatus(status), HttpStatus.OK);
    }

    @GetMapping("/current")
    public ResponseEntity<Page<OrderResponseDto>> getOrdersByUserEmail(
            @RequestHeader("X-User-Id") String userEmail,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return new ResponseEntity<>(orderService.getOrdersByUserEmail(userEmail, page, size), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody OrderRequestDto orderRequestDto){
        return new ResponseEntity<>(orderService.createOrder(orderRequestDto), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/payment")
    public ResponseEntity<OrderResponseDto> payOrder(@PathVariable Long id) {
        return new ResponseEntity<>(orderService.payOrder(id), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponseDto> updateOrder(
            @PathVariable Long id, @Valid @RequestBody OrderRequestDto orderRequestDto){
        return new ResponseEntity<>(orderService.updateOrderById(id, orderRequestDto), HttpStatus.OK);
    }

    @PutMapping("/{id}/address")
    public ResponseEntity<OrderResponseDto> updateShippingAddress(
            @PathVariable Long id,
            @Valid @RequestBody UpdateShippingAddressRequestDto requestDto
    ) {
        return new ResponseEntity<>(orderService.updateShippingAddress(id, requestDto), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id){
        orderService.deleteOrderById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/timeline")
    public ResponseEntity<List<OrderEventResponseDto>> getOrderHistory(@PathVariable Long id) {
        return new ResponseEntity<>(orderService.getOrderHistory(id), HttpStatus.OK);
    }

    @PostMapping("/{id}/restoration")
    public ResponseEntity<OrderResponseDto> restoreOrderStatusAt(@PathVariable Long id,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date
    ) {
        return new ResponseEntity<>(orderService.restoreOrderStatusAt(id, date), HttpStatus.OK);
    }
}


