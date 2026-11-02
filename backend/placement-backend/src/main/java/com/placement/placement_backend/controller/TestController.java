
package com.placement.placement_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping
    public String test() {
        return "Placement Preparation Platform Backend is working!";
    }

    @GetMapping("/protected")
    public String protectedTest() {
        return "JWT authentication test passed!";
    }
}
