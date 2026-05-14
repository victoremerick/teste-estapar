package com.estapar.controller;

import com.estapar.dto.RevenueResponse;
import com.estapar.service.RevenueService;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RevenueController {

    private final RevenueService revenueService;

    public RevenueController(RevenueService revenueService) {
        this.revenueService = revenueService;
    }

    @GetMapping("/revenue")
    public RevenueResponse revenue(@RequestParam LocalDate date, @RequestParam String sector) {
        return revenueService.getRevenue(date, sector);
    }
}
