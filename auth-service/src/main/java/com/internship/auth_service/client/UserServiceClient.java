package com.internship.auth_service.client;

import com.internship.auth_service.config.FeignConfig;
import com.internship.auth_service.dto.UserServiceRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "user-service",
        url = "${user.service.url:http://user-service:8080}",
        configuration = FeignConfig.class
)
public interface UserServiceClient {

    @PostMapping("/api/v1/users")
    void registerUser(@RequestBody UserServiceRequest userRequestDTO);

    @DeleteMapping("/api/v1/users/email/{email}")
    void deleteUserByEmail(@PathVariable String email);
}