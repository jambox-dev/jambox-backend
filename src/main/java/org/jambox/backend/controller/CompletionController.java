package org.jambox.backend.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/completion")
public class CompletionController {
    @GetMapping
    public String[] getCompletion(@RequestBody String search) {

        return new String[] {"hello", "world"};
    }
}
