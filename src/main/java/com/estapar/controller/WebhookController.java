package com.estapar.controller;

import com.estapar.dto.WebhookEventRequest;
import com.estapar.service.ParkingEventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private final ParkingEventService parkingEventService;

    public WebhookController(ParkingEventService parkingEventService) {
        this.parkingEventService = parkingEventService;
    }

    @PostMapping
    public ResponseEntity<Void> webhook(@RequestBody WebhookEventRequest request) {
        try {
            parkingEventService.handleEvent(request);
            return ResponseEntity.ok().build();
        } catch (ResponseStatusException ex) {
            if (ex.getStatusCode().value() == HttpStatus.CONFLICT.value()) {
                throw ex;
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getReason(), ex);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }
}
