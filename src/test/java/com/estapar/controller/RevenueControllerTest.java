package com.estapar.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.estapar.dto.RevenueResponse;
import com.estapar.service.RevenueService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RevenueController.class)
class RevenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RevenueService revenueService;

    @Test
    void shouldReturnRevenueResponse() throws Exception {
        when(revenueService.getRevenue(LocalDate.of(2025, 1, 1), "A"))
                .thenReturn(new RevenueResponse(
                        new BigDecimal("100.00"),
                        "BRL",
                        Instant.parse("2025-01-01T12:00:00.000Z")));

        mockMvc.perform(get("/revenue")
                        .param("date", "2025-01-01")
                        .param("sector", "A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.currency").value("BRL"))
                .andExpect(jsonPath("$.timestamp").value("2025-01-01T12:00:00Z"));
    }
}
