package com.donorconnect.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api")
@Tag(name = "System Health", description = "Public health check and welcome endpoint")
public class HelloController {

    @GetMapping("/hello")
    @Operation(summary = "Public welcome endpoint", description = "Returns system welcome message without authentication.")
    @ApiResponse(responseCode = "200", description = "System active and responding")
    public Map<String, String> getHello() {
        return Collections.singletonMap("message", "Welcome to DonorConnect");
    }
}
