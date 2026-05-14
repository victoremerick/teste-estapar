package com.estapar.controller;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.estapar.service.ParkingEventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(WebhookController.class)
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ParkingEventService parkingEventService;

    @Test
    void shouldReturnOkWhenEventIsProcessed() throws Exception {
        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "license_plate": "ZUL0001",
                                  "entry_time": "2025-01-01T12:00:00.000Z",
                                  "event_type": "ENTRY"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnConflictForBusinessConflict() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Spot already occupied"))
                .when(parkingEventService).handleEvent(org.mockito.ArgumentMatchers.any());

        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "license_plate": "ZUL0001",
                                  "lat": -23.561684,
                                  "lng": -46.655981,
                                  "event_type": "PARKED"
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldConvertNotFoundBusinessErrorToBadRequest() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Active stay not found"))
                .when(parkingEventService).handleEvent(org.mockito.ArgumentMatchers.any());

        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "license_plate": "ZUL0001",
                                  "exit_time": "2025-01-01T12:00:00.000Z",
                                  "event_type": "EXIT"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
