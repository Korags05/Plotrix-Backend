package com.oneneev.plotrix.controller;

import com.oneneev.plotrix.dto.SignalRequest;
import com.oneneev.plotrix.service.SignalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/signals")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SignalController {
    private final SignalService signalService;

    @PostMapping
    public ResponseEntity<String> ingest(@Valid @RequestBody SignalRequest request) {
        signalService.process(request);
        return ResponseEntity.ok("Signal recorded");
    }
}
