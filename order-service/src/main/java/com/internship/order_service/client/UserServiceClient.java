package com.internship.order_service.client;

import com.internship.order_service.dto.UserInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${user.service.url:http://localhost:8080}")
public interface UserServiceClient {

    @GetMapping("/api/v1/users/email/{email}")
    UserInfoDTO getUserInfoByEmail(@PathVariable String email);

}
