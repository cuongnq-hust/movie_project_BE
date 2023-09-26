package dev.cuongnq.moives.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
public class DemoController {
    @GetMapping("/admin")
    public ResponseEntity<?> admin(){
        return ResponseEntity.ok("this is Admin route");
    }
    @GetMapping("/user")
    public ResponseEntity<?> users(){
        return ResponseEntity.ok("this is User route");
    }
}
